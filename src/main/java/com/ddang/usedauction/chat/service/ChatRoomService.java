package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.repository.ChatMessageRepository;
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
    private final ChatMessageRepository chatMessageRepository;

    // 채팅방(topic)에 발행되는 메시지 처리하는 Listener
    private final RedisMessageListenerContainer redisMessageListener;
    // 구독 처리 서비스
    private final RedisSubscriber redisSubscriber;
    // Redis와 관련된 데이터 작업 수행
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Integer> unReadRedisTemplate;
    private HashOperations<String, String, ChatRoomCreateDto.Response> opsHashChatRoom;
    // 서버별로 채팅방에 매치되는 topic 정보를 Map에 넣어 roomId로 찾을 수 있음
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    /**
     * 회원이 속한 모든 채팅방 조회
     */
    public List<ChatRoomCreateDto.Response> findChatRoomsByMemberId(String memberId) {

        return opsHashChatRoom.values(CHAT_ROOMS).stream()
            .map(chatRoom -> {

                String unreadKey = "CHAT_ROOM" + chatRoom.getId() + "_UN_READ:" + memberId;
                Integer unreadCnt = unReadRedisTemplate.opsForValue().get(unreadKey);
                unreadCnt = unreadCnt == null ? 0 : unreadCnt; // null 체크하여 기본값 0 설정

                ChatMessage chatMessage = chatMessageRepository.findTop1ByChatRoomId(
                    chatRoom.getId()).orElse(null);

                // Response 객체 생성 시 필요한 값들 설정
                return ChatRoomCreateDto.Response.builder()
                    .id(chatRoom.getId())
                    .buyer(chatRoom.getBuyer())
                    .seller(chatRoom.getSeller())
                    .auction(chatRoom.getAuction())
                    .unReadCnt(unreadCnt)
                    .lastMessage(chatMessage != null ? chatMessage.getMessage() : null)
                    .lastMessageTime(chatMessage != null ? chatMessage.getCreatedAt() : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 채팅방 생성 (판매자, 구매자, 해당 경매)
     */
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

    /**
     * 채팅방 입장
     */
    public void enterChatRoom(Long roomId, String memberId) {
        redisTemplate.opsForSet().add("CHAT_ROOM" + roomId + "_MEMBERS:", memberId);

    }

    /**
     * 채팅방 퇴장
     */
    public void exitChatRoom(Long roomId, String memberId) {
        redisTemplate.opsForSet()
            .remove("CHAT_ROOM" + roomId + "_MEMBERS:", memberId);
    }

    /**
     * 경매 제목으로 회원이 속한 채팅방 조회
     */
    public List<ChatRoomCreateDto.Response> searchChatRoomByAuctionTitle(String title) {
        return chatRoomRepository.findByAuctionTitle(title).stream()
            .map(ChatRoomCreateDto.Response::from)
            .collect(Collectors.toList());
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
