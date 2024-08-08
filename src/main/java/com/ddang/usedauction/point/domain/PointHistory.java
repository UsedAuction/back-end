package com.ddang.usedauction.point.domain;

import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.point.type.PointType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity(name = "point_history")
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType pointType; // 포인트 타입

    @Column(nullable = false)
    private long pointAmount; // 충전 or 사용 포인트량

    @Column(nullable = false)
    private long curPointAmount; // 현재 보유 포인트량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원
}
