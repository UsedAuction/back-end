package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.repository.ChatRoomRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private static final String CHAT_ROOMS = "CHAT_ROOM";

    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 채팅방(topic)에 발행되는 메시지 처리하는 Listener
    private final RedisMessageListenerContainer redisMessageListener;
    // 구독 처리 서비스
    private final RedisSubscriber redisSubscriber;
    // Redis와 관련된 데이터 작업 수행
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoomCreateDto.Response> opsHashChatRoom;
    // 서버별로 채팅방에 매치되는 topic 정보를 Map에 넣어 roomId로 찾을 수 있음
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    public List<ChatRoomCreateDto.Response> findChatRoomsByMemberId(String memberId) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return opsHashChatRoom.values(CHAT_ROOMS).stream()
            .filter(chatRoom -> chatRoom.getSeller().getMemberId().equals(member.getMemberId()) ||
                chatRoom.getBuyer().getMemberId().equals(member.getMemberId()))
            .collect(Collectors.toList());
    }

    public void createChatRoom(Long memberId, Long auctionId) {
        if (chatRoomRepository.existsByAuctionId(auctionId)) {
            throw new IllegalStateException("이미 존재하는 채팅방입니다.");
        }

        Member buyer = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
            .seller(auction.getSeller())
            .buyer(buyer)
            .auction(auction)
            .build());

        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getId().toString(),
            ChatRoomCreateDto.Response.from(chatRoom));

        createTopic(chatRoom.getId().toString());
    }

    public ChatRoom deleteChatRoom(Long auctionId) {

        ChatRoom chatRoom = chatRoomRepository.findByAuctionId(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅방입니다"));

        opsHashChatRoom.delete(CHAT_ROOMS, chatRoom.getId().toString());

        chatRoom.exitChatRoom();

        deleteTopic(chatRoom.getId().toString());

        return chatRoom;
    }

    public ChannelTopic getTopic(Long roomId) {
        return topics.get(roomId.toString());
    }

    private void createTopic(String roomId) {
        if (!topics.containsKey(roomId)) {
            ChannelTopic topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }

    private void deleteTopic(String roomId) {
        ChannelTopic topic = topics.get(roomId);

        redisMessageListener.removeMessageListener(redisSubscriber, topic);
        topics.remove(roomId);
    }
}
