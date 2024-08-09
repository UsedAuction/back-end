package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.Member.Member;
import com.ddang.usedauction.Member.MemberRepository;
import com.ddang.usedauction.Member.exception.MemberErrorCode;
import com.ddang.usedauction.Member.exception.MemberException;
import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.exception.ChatErrorCode;
import com.ddang.usedauction.chat.exception.ChatException;
import com.ddang.usedauction.chat.repository.ChatMessageRepository;
import com.ddang.usedauction.chat.repository.ChatRoomRepository;
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
  public ChatMessageSendDto.Response sendMessage(ChatMessageSendDto.Request message) {
    Member member = memberRepository.findById(message.getSenderId())
        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

    ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
        .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_ROOM));
    ChatMessage chatMessage = ChatMessage.builder()
        .message(message.getMessage())
        .sender(member)
        .chatRoom(chatRoom)
        .build();

    return ChatMessageSendDto.Response.of(chatMessageRepository.save(chatMessage));
  }
}
