package com.ddang.usedauction.member.domain;

import com.ddang.usedauction.config.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@SQLRestriction("deleted_at IS NULL")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 6, max = 12)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용 가능합니다.")
    @Column(nullable = false, length = 20, unique = true)
    private String memberId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 4, max = 16)
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{4,16}$",
            message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
    )
    @JsonProperty
    @Column(nullable = false)
    private String passWord;

    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Column(nullable = false, unique = true)
    private String email;

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

    public void update(Member newMember) {
        memberId = newMember.memberId;
        passWord = newMember.passWord;
        email = newMember.email;
    }
    public boolean isValidPassWord(String password) {
        return this.passWord.equals(password);
    }

    @Column
    private boolean siteAlarm;

    @Column
    private long point;

    @Column
    private String social;

    @Column
    private String socialProviderId;

    @Column
    private LocalDateTime deletedAt;

    // 포인트 충전
    public void addPoint(int point) {
        this.point += point;
    }
}
