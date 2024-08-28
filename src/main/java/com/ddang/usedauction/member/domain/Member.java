package com.ddang.usedauction.member.domain;

import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.member.dto.MemberServiceDto;
import com.ddang.usedauction.member.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

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

    @Column(nullable = false, length = 12, unique = true)
    private String memberId;

    @Column(nullable = false)
    private String passWord;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private boolean siteAlarm;

    @Column
    private long point;

    @Column
    private String social;

    @Column
    private String socialProviderId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateMemberId(String newMemberId) {
        this.memberId = newMemberId;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updatePassword(String newPassword) {
        this.passWord = newPassword;
    }

    public boolean isValidPassWord(String password) {
        return this.passWord.equals(password);
    }

    /**
     * Entity -> ServiceDto
     *
     * @return ServiceDto
     */
    public MemberServiceDto toServiceDto() {
        return MemberServiceDto.builder()
                .id(id)
                .memberId(memberId)
                .passWord(passWord)
                .email(email)
                .role(role)
                .createDate(getCreatedAt())
                .updateDate(getUpdatedAt())
                .deleteDate(deletedAt)
                .build();
    }

    // 포인트 충전
    public void addPoint(int point) {
        this.point += point;
    }
}
