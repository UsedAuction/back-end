package com.ddang.usedauction.order.domain;

import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity(name = "orders")
public class Orders extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 주문 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원 아이디

    @Column(nullable = false)
    private String itemName; // 아이템명

    @Column(nullable = false)
    @Min(value = 1, message = "아이템 가격은 1이상이어야 합니다.")
    private int price; // 아이템 가격

    @Setter
    private String tid; // 결제 고유 번호 (결제준비요청 후 받는 응답에 존재. 결제승인처리 로직에 필요. 초기값은 null)
}
