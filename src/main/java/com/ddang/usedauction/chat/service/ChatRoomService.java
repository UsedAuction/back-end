package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.Member.Member;
import com.ddang.usedauction.Member.MemberRepository;
import com.ddang.usedauction.Member.exception.MemberErrorCode;
import com.ddang.usedauction.Member.exception.MemberException;
import com.ddang.usedauction.auction.Auction;
import com.ddang.usedauction.auction.AuctionRepository;
import com.ddang.usedauction.auction.exception.AuctionErrorCode;
import com.ddang.usedauction.auction.exception.AuctionException;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.exception.ChatErrorCode;
import com.ddang.usedauction.chat.exception.ChatException;
import com.ddang.usedauction.chat.repository.ChatRoomRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public ChatRoomCreateDto.Response createChatRoom(Long memberId, ChatRoomCreateDto.Request dto) {
    if (chatRoomRepository.existsByAuctionId(dto.getAuctionId())) {
      throw new ChatException(ChatErrorCode.ALREADY_EXISTS_CHATROOM);
    }

    Member buyer = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

    Auction auction = auctionRepository.findById(dto.getAuctionId())
        .orElseThrow(() -> new AuctionException(AuctionErrorCode.NOT_FOUND_AUCTION));

    ChatRoom chatRoom = ChatRoom.builder()
        .seller(auction.getMember())
        .buyer(buyer)
        .auction(auction)
        .build();
    chatRoomRepository.save(chatRoom);

    createTopic(chatRoom.getId().toString());

    opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getId().toString(),
        ChatRoomCreateDto.Response.of(chatRoom));

    return ChatRoomCreateDto.Response.of(chatRoomRepository.save(chatRoom));
  }

  public List<ChatRoomCreateDto.Response> findChatRoomsByMemberId(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

    return opsHashChatRoom.values(CHAT_ROOMS).stream()
        .filter(chatRoom -> chatRoom.getSeller().getId().equals(memberId) ||
            chatRoom.getBuyer().getId().equals(memberId))
        .collect(Collectors.toList());
  }

  public ChatRoomCreateDto.Response findRoomById(Long roomId) {
    return opsHashChatRoom.get(CHAT_ROOMS, roomId);
  }

  public void enterChatRoom(Long roomId) {
    String roomIdStr = roomId.toString();
    ChannelTopic topic = topics.get(roomId);
    if (topic == null) {
      topic = new ChannelTopic(roomIdStr);
      redisMessageListener.addMessageListener(redisSubscriber, topic);
      topics.put(roomIdStr, topic);
    }
  }

  public ChannelTopic getTopic(Long roomId) {
    return topics.get(roomId.toString());
  }

  private void createTopic(String roomId) {
    if (!topics.containsKey(roomId)) {
      ChannelTopic topic = new ChannelTopic(roomId);
      redisMessageListener.addMessageListener(redisSubscriber, topic);
      topics.put(roomId, topic);
      log.info("생성된 토픽 : {}", topics.get(roomId));
    }
  }
}
