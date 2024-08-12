package com.ddang.usedauction.Member;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

public class MemberDto {

  @Getter
  @Builder
  public static class Response implements Serializable {

    private Long id;
    private String memberId;
    private String email;

    public static Response from(Member member) {
      return Response.builder()
          .id(member.getId())
          .memberId(member.getMemberId())
          .email(member.getEmail()).build();
    }
  }
}
