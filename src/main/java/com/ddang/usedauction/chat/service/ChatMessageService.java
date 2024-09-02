package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.exception.UnauthorizedAccessException;
import com.ddang.usedauction.chat.repository.ChatMessageRepository;
import com.ddang.usedauction.chat.repository.ChatRoomRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final SimpMessageSendingOperations sendingOperations;

    /**
     * 메시지 저장 Service
     *
     * @param request (roomId, senderId, message)
     */
    @Transactional
    public void sendMessage(ChatMessageSendDto.Request request) {
        Member member = memberRepository.findByMemberId(request.getSenderId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅방입니다."));

        ChatMessage chatMessage = ChatMessage.builder()
            .message(request.getMessage())
            .sender(member)
            .chatRoom(chatRoom)
            .build();

        chatMessageRepository.save(chatMessage);
        sendingOperations.convertAndSend("/sub/chat/room/" + request.getRoomId(), request);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageSendDto.Response> findMessagesByChatRoomId(String memberId,
        Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅방입니다."));

        // 로그인한 사용자가 채팅방에 속해 있는지 확인
        if (!isMemberOfChatRoom(chatRoom, memberId)) {
            throw new UnauthorizedAccessException();
        }

        return chatMessageRepository.findByChatRoomId(chatRoomId).stream()
            .map(ChatMessageSendDto.Response::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMessagesByChatRoom(Long chatRoomId) {
        chatMessageRepository.deleteChatMessageByChatRoomId(chatRoomId);
    }

    private boolean isMemberOfChatRoom(ChatRoom chatRoom, String memberId) {
        return chatRoom.getSeller().getMemberId().equals(memberId) ||
            chatRoom.getBuyer().getMemberId().equals(memberId);
    }
}
