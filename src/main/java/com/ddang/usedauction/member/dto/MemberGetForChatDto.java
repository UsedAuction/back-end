package com.ddang.usedauction.member.dto;

import com.ddang.usedauction.member.domain.Member;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class MemberGetForChatDto implements Serializable {

    private String memberId;
    private String social;

    public static MemberGetForChatDto from(Member member) {
        return MemberGetForChatDto.builder()
            .memberId(member.getMemberId())
            .social(member.getSocial())
            .build();
    }
}