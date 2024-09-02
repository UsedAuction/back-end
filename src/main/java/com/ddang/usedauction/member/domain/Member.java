package com.ddang.usedauction.member.domain;

import com.ddang.usedauction.config.BaseTimeEntity;
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

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String passWord;

    @Column(nullable = false)
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

    @Column
    private LocalDateTime deletedAt;

    @Column
    private String withdrawalReason; // 탈퇴사유

    // 포인트 충전
    public void addPoint(int point) {
        this.point += point;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.passWord = password;
    }

    public void withdrawal(String withDrawalReason) {
        this.deletedAt = LocalDateTime.now();
        this.withdrawalReason = withDrawalReason;
    }
}
