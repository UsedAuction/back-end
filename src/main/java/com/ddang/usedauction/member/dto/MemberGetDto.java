package com.ddang.usedauction.member.dto;

import com.ddang.usedauction.member.domain.Member;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberGetDto {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Builder(toBuilder = true)
  public static class Response implements Serializable {

    private Long id;
    private String memberId;
    private String passWord;
    private String email;
    private boolean siteAlarm;
    private long point;
    private String social;
    private String socialProviderId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;


    public String getMemberId() {
      return memberId;
    }

    public String getPassWord() {
      return passWord;
    }


    public String getEmail() {
      return email;
    }

    public void setMemberId(String memberId) {
      this.memberId = memberId;
    }

    public void setPassWord(String passWord) {
      this.passWord = passWord;
    }

    public void setEmail(String email) {
      this.email = email;
    }
    public boolean isValidPassWord(String password) {
      return this.passWord.equals(password);
    }

    // entity -> getResponse
    public static MemberGetDto.Response from(Member member) {

      return Response.builder()
          .id(member.getId())
          .memberId(member.getMemberId())
          .passWord(member.getPassWord())
          .email(member.getEmail())
          .siteAlarm(member.isSiteAlarm())
          .point(member.getPoint())
          .social(member.getSocial())
          .socialProviderId(member.getSocialProviderId())
          .createdAt(member.getCreatedAt())
          .deletedAt(member.getDeletedAt())
          .build();
    }

  }
}
