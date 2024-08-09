package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  
  boolean existsByAuctionId(Long auctionId);
}
