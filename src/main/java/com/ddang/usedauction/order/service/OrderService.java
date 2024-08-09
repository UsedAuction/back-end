package com.ddang.usedauction.order.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // 주문 생성
    public OrderCreateDto.Response createOrder(OrderCreateDto.Request request) {

        Member member = Member.builder()
            .id(1L)
            .build(); // TODO 토큰을 받아 처리하는 것으로 수정

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
