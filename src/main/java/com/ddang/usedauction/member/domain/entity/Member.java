package com.ddang.usedauction.member.domain.entity;

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

  @Column(nullable = false)
  private String memberId;

  @Column(nullable = false)
  private String passWord;

  @Column(nullable = false)
  private String email;

  @Column
  private boolean siteAlarm;

  @Column
  private long point;

  @Column
  @Enumerated(EnumType.STRING)
  private Role role;

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