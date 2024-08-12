package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.auction.dto.AuctionGetDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.member.dto.MemberGetDto;
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
    private MemberGetDto.Response seller;
    private MemberGetDto.Response buyer;
    private AuctionGetDto.Response auction;

    public static Response from(ChatRoom chatRoom) {
      return Response.builder()
          .id(chatRoom.getId())
          .seller(MemberGetDto.Response.from(chatRoom.getSeller()))
          .buyer(MemberGetDto.Response.from(chatRoom.getBuyer()))
          .auction(AuctionGetDto.Response.from(chatRoom.getAuction()))
          .build();
    }
  }
}
