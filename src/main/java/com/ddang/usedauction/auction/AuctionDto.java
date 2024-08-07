package com.ddang.usedauction.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AuctionDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class Response {

    private Long id;
    private String title;
    private String description;
    private String memberId;

    public static Response of(Auction auction) {
      return Response.builder()
          .id(auction.getId())
          .title(auction.getTitle())
          .description(auction.getDescription())
          .memberId(auction.getMember().getMemberId())
          .build();
    }
  }

}
