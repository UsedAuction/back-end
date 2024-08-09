package com.ddang.usedauction.chat.repository;

import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

//  @Query("SELECT c FROM ChatRoom c WHERE c.seller.id = :memberId OR c.buyer.id = :memberId")
//  List<ChatRoom> findChatRoomsByMemberId(@Param("memberId") Long memberId);

  boolean existsByAuctionId(Long auctionId);
}
