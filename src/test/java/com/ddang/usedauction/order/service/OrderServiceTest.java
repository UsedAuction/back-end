package com.ddang.usedauction.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.repository.OrderRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Disabled
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrderSuccess() {
        //given
        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(10000)
            .build();

        //when
        Orders result = orderService.createOrder(request);

        //then
        assertNotNull(result);
        assertEquals("10000 포인트", result.getItemName());
        assertEquals(10000, result.getPrice());

        verify(orderRepository, times(1)).save(result);
    }

    @Test
    @DisplayName("주문 생성 - 실패")
    void createOrderFail_saveException() {
        //given
        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(0)
            .build();

        Orders order = Orders.builder()
            .member(Member.builder().id(1L).build())
            .price(10000)
            .build();

        given(orderRepository.save(order)).willThrow(new RuntimeException("DB 저장 실패"));

        // when
        // then
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }
}