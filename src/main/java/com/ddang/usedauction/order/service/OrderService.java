package com.ddang.usedauction.order.service;

import static com.ddang.usedauction.member.exception.MemberErrorCode.NOT_FOUND_MEMBER;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

//    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    // 주문 생성
    public OrderCreateDto.Response createOrder(OrderCreateDto.Request request) {

        // 아래 코드는 테스트 용이성을 위해 주석처리. 회원기능 구현 완료시 수정 예정
        // (UserDetails userDetails, OrderCreateDto.Request request) {

        // String email = userDetails.getUsername();
        // Member member = memberRepository.findByEmail(email)
        //    .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        Member member = Member.builder()
            .id(1L)
            .build();

        String itemName = request.getPrice() + " 포인트";

        Orders order = Orders.builder()
            .member(member)
            .itemName(itemName)
            .price(request.getPrice())
            .build();

        orderRepository.save(order);

        return OrderCreateDto.Response.fromEntity(order);
    }
}
