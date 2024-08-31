package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRomeId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deletedAt = CURRENT_TIMESTAMP WHERE  m.chatRoom.id = :chatRoomId")
    void deleteChatMessageByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
