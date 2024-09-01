package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("select cm from ChatMessage cm where cm.chatRoom.id = :chatRoomId order by cm.createdAt asc")
    List<ChatMessage> findByChatRoomId(Long chatRoomId);
}
