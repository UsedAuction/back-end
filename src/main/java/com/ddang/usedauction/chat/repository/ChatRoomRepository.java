package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByAuctionId(Long auctionId);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.auction a WHERE LOWER(a.title) LIKE %:title%")
    List<ChatRoom> findByAuctionTitle(@Param("title") String title);
}
