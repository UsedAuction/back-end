package com.ddang.usedauction.order.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.repository.OrderRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    // 주문 생성
    public Orders createOrder(String email, OrderCreateDto.Request request) {

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        String itemName = request.getPrice() + " 포인트";

        Orders order = Orders.builder()
            .member(member)
            .itemName(itemName)
            .price(request.getPrice())
            .build();

        orderRepository.save(order);

        return order;
    }
}
