package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByAuctionId(Long auctionId);

    boolean existsByAuctionId(Long auctionId);
}
