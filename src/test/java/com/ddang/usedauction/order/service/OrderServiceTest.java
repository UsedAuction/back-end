package com.ddang.usedauction.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.repository.OrderRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrderSuccess() {
        //given
        Member member = Member.builder()
            .id(1L)
            .memberId("memberId")
            .email("test@naver.com")
            .build();

        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(10000)
            .build();

        given(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).willReturn(
            Optional.of(member));

        //when
        Orders result = orderService.createOrder(member.getMemberId(), request);

        //then
        assertNotNull(result);
        assertEquals("10000 포인트", result.getItemName());
        assertEquals(10000, result.getPrice());

        verify(memberRepository, times(1)).findByMemberIdAndDeletedAtIsNull(member.getMemberId());
        verify(orderRepository, times(1)).save(result);
    }

    @Test
    @DisplayName("주문 생성 - 실패(유저 존재 x)")
    void createOrderFail_1() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(1000)
            .build();

        given(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).willReturn(
            Optional.empty());

        // when
        // then
        assertThrows(NoSuchElementException.class,
            () -> orderService.createOrder(member.getMemberId(), request));
    }

    @Test
    @DisplayName("주문 생성 - 실패(주문 저장 실패)")
    void createOrderFail_2() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(1000)
            .build();

        Orders order = Orders.builder()
            .member(Member.builder().id(1L).build())
            .price(10000)
            .build();

        given(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).willReturn(
            Optional.of(member));
        given(orderRepository.save(order)).willThrow(new RuntimeException("DB 저장 실패"));

        // when
        // then
        verify(memberRepository, times(0)).findByEmailAndDeletedAtIsNull(member.getMemberId());
        assertThrows(RuntimeException.class,
            () -> orderService.createOrder(member.getMemberId(), request));
    }
}