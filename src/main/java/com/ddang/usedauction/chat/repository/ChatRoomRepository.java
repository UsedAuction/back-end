package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  @Query("SELECT c FROM ChatRoom c WHERE c.seller.id = :memberId OR c.buyer.id = :memberId")
  List<ChatRoom> findChatRoomsByMemberId(@Param("memberId") Long memberId);

  boolean existsByAuctionId(Long auctionId);
}
