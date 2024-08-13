package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.repository.ChatMessageRepository;
import com.ddang.usedauction.chat.repository.ChatRoomRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final MemberRepository memberRepository;

  /**
   * 메시지 저장 Service
   *
   * @param message (roomId, senderId, message)
   */
  @Transactional
  public ChatMessage sendMessage(ChatMessageSendDto.Request message) {
    Member member = memberRepository.findById(message.getSenderId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

    ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅방입니다."));
    ChatMessage chatMessage = ChatMessage.builder()
        .message(message.getMessage())
        .sender(member)
        .chatRoom(chatRoom)
        .build();

    return chatMessageRepository.save(chatMessage);
  }
}
