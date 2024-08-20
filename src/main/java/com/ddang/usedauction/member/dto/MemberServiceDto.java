package com.ddang.usedauction.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MemberServiceDto implements Serializable {

    private Long id; // pk
    private String memberId; // 아이디
    private String passWord; // 패스워드
    private String email; // 이메일
    private LocalDateTime createDate; // 생성 날짜
    private LocalDateTime updateDate; // 수정 날짜
    private LocalDateTime deleteDate; // 삭제 날짜

    /**
     * ServiceDto -> VerifyResponse
     *
     * @return VerifyResponse
     */
    public MemberGetDto.Response toGetResponse() {

        return MemberGetDto.Response.builder()
                .memberId(memberId)
                .passWord(passWord)
                .email(email)
                .build();
    }

    /**
     * ServiceDto -> ModifyResponse
     *
     * @return ModifyResponse
     */
    public MemberUpdateDto.Response toUpdateResponse() {

        return MemberUpdateDto.Response.builder()
                .memberId(memberId)
                .passWord(passWord)
                .email(email)
                .build();
    }
}
