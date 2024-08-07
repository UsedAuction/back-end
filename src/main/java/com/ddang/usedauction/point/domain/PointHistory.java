package com.ddang.usedauction.point.domain;

import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.member.Member;
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

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    // 충전 or 사용 포인트량
    private int pointAmount;

    // 현재 보유 포인트량
    private int curPointAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
