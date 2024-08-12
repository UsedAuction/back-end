package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.Member.MemberDto;
import com.ddang.usedauction.auction.AuctionDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRoomCreateDto {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Builder
  public static class Request implements Serializable {

    private Long auctionId;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Builder
  public static class Response implements Serializable {

    private Long id;
    private MemberDto.Response seller;
    private MemberDto.Response buyer;
    private AuctionDto.Response auction;

    public static Response from(ChatRoom chatRoom) {
      return Response.builder()
          .id(chatRoom.getId())
          .seller(MemberDto.Response.from(chatRoom.getSeller()))
          .buyer(MemberDto.Response.from(chatRoom.getBuyer()))
          .auction(AuctionDto.Response.from(chatRoom.getAuction()))
          .build();
    }
  }
}
