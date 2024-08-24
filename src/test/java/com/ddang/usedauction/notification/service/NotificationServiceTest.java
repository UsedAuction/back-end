package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
@Disabled
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private NotificationService notificationService;

    String lastEventId;
    private Member seller;
    private Auction auction;

    @BeforeEach
    void before() {

        lastEventId = "123";

        seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .bidList(null)
            .build();
    }

    @Test
    @DisplayName("알림 구독 - 성공")
    void subscribeSuccess() {
        //given
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(sseEmitter);

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SseEmitter> emitterCaptor = ArgumentCaptor.forClass(SseEmitter.class);

        //when
        SseEmitter resultEmitter = notificationService.subscribe(seller.getId(), lastEventId);

        //then
        assertNotNull(resultEmitter);
        assertEquals(sseEmitter, resultEmitter);
        verify(emitterRepository).save(emitterIdCaptor.capture(), emitterCaptor.capture());
    }

    @Test
    @DisplayName("알림 구독 - 실패 (emitterRepository 저장 실패)")
    void subscribeFail_emitterRepositorySave() {
        //given
        given(emitterRepository.save(anyString(), any(SseEmitter.class)))
            .willThrow(new RuntimeException());

        //when
        //then
        assertThrows(RuntimeException.class,
            () -> notificationService.subscribe(seller.getId(), lastEventId));
    }

    @Test
    @DisplayName("알림 구독 - 실패 (sendNotification 에서 전송 실패)")
    void subscribeFail_sendNotification() throws Exception {
        //given
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(sseEmitter);
        doThrow(new IOException()).when(sseEmitter).send(any(SseEmitter.SseEventBuilder.class));

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);

        // when
        notificationService.subscribe(seller.getId(), lastEventId);

        // then
        verify(emitterRepository, times(1)).deleteById(emitterIdCaptor.capture());
    }

    @Test
    @DisplayName("알림 구독 - 실패 (findAllEventCacheStartWithMemberId 조회 실패)")
    void subscribeFail_findAllEventCacheStartWithMemberId() {
        //given
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(sseEmitter);
        given(emitterRepository.findAllEventCacheStartWithMemberId(String.valueOf(seller.getId())))
            .willThrow(new RuntimeException("findAllEventCacheStartWithMemberId 실패"));

        //when
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> notificationService.subscribe(seller.getId(), lastEventId));

        //then
        assertEquals("findAllEventCacheStartWithMemberId 실패", e.getMessage());
    }

    @Test
    @DisplayName("알림 전송 - 성공")
    void sendSuccess() {
        //given
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(seller)
            .build();

        Map<String, SseEmitter> emitters = new HashMap<>();
        SseEmitter sseEmitter1 = new SseEmitter();
        SseEmitter sseEmitter2 = new SseEmitter();
        emitters.put("1_1", sseEmitter1);
        emitters.put("1_2", sseEmitter2);

        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId("1")).willReturn(emitters);
        given(memberRepository.findById(1L)).willReturn(Optional.of(seller));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));

        //when
        notificationService.send(seller.getId(), auction.getId(), content, notificationType);

        //then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(2)).saveEventCache(anyString(), any(Notification.class));
        verify(memberRepository, times(1)).findById(1L);
        verify(auctionRepository, times(1)).findById(auction.getId());
    }

    @Test
    @DisplayName("알림 전송 - 실패 (memberRepository 회원 조회 실패)")
    void sendFail_memberRepository() {
        //given
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        given(memberRepository.findById(seller.getId())).willReturn(Optional.empty());

        //when
        NoSuchElementException e = assertThrows(NoSuchElementException.class,
            () -> notificationService.send(seller.getId(), auction.getId(), content,
                notificationType));

        //then
        assertEquals("존재하지 않는 회원입니다.", e.getMessage());
        verify(notificationRepository, times(0)).save(any(Notification.class));
        verify(emitterRepository, times(0)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(0)).saveEventCache(anyString(), any(Notification.class));
        verify(auctionRepository, times(0)).findById(auction.getId());
    }

    @Test
    @DisplayName("알림 전송 - 실패 (findAllEmitterStartWithMemberId 찾기 실패)")
    void sendFail_findAllEmitterStartWithMemberId() {
        //given
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(seller)
            .build();

        given(memberRepository.findById(seller.getId())).willReturn(Optional.of(seller));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId(
            String.valueOf(seller.getId()))).willThrow(new RuntimeException("찾기 실패"));

        //when
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> notificationService.send(seller.getId(), auction.getId(), content,
                notificationType));

        //then
        assertEquals("찾기 실패", e.getMessage());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId(
            String.valueOf(seller.getId()));
        verify(emitterRepository, times(0)).saveEventCache(anyString(), any(Notification.class));
    }

    @Test
    @DisplayName("알림 전송 - 실패 (saveEventCache 저장 실패)")
    void sendFail_saveEventCache() {
        //given
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(seller)
            .build();

        SseEmitter emitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = Collections.singletonMap("1_1234", emitter);

        given(memberRepository.findById(seller.getId())).willReturn(Optional.of(seller));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId(
            String.valueOf(seller.getId()))).willReturn(emitters);
        doThrow(new RuntimeException("저장 실패")).when(emitterRepository)
            .saveEventCache(anyString(), any(Notification.class));

        //when
        assertThrows(RuntimeException.class, () -> {
            notificationService.send(seller.getId(), auction.getId(), content, notificationType);
        });

        //then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(1)).saveEventCache(anyString(), any(Notification.class));
    }

    @Test
    @DisplayName("알림 전송 - 실패 (sendNotification 에서 전송 실패)")
    void sendFail_sendNotification() throws IOException {
        //given
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(seller)
            .build();

        SseEmitter emitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = Collections.singletonMap("1_1234", emitter);

        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId("1")).willReturn(emitters);
        given(memberRepository.findById(1L)).willReturn(Optional.of(seller));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));

        doThrow(new IOException("전송 실패")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);

        //when
        notificationService.send(seller.getId(), auction.getId(), content, notificationType);

        // then
        verify(emitterRepository, times(1)).deleteById(emitterIdCaptor.capture());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(1)).saveEventCache(anyString(), any(Notification.class));
        verify(emitterRepository, times(1)).deleteById("1_1234");
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 성공")
    void getNotificationListSuccess() {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        Page<Notification> notificationPage = new PageImpl<>(
            List.of(
                Notification.builder()
                    .id(1L)
                    .content("알림1")
                    .notificationType(NotificationType.DONE)
                    .member(seller)
                    .build(),
                Notification.builder()
                    .id(2L)
                    .content("알림2")
                    .notificationType(NotificationType.CONFIRM)
                    .member(seller)
                    .build(),
                Notification.builder()
                    .id(3L)
                    .content("알림3")
                    .notificationType(NotificationType.QUESTION)
                    .member(seller)
                    .build()),
            pageable,
            3
        );

        given(memberRepository.findById(seller.getId())).willReturn(Optional.of(seller));
        given(notificationRepository.findNotificationList(eq(seller.getId()),
            any(LocalDateTime.class), eq(pageable)))
            .willReturn(notificationPage);

        //when
        Page<Notification> result = notificationService.getNotificationList(seller.getId(),
            pageable);

        //then
        assertEquals(notificationPage.getContent().size(), result.getTotalElements());
        assertEquals(notificationPage.getContent().get(0).getContent(),
            result.getContent().get(0).getContent());
        assertEquals(notificationPage.getContent().get(1).getContent(),
            result.getContent().get(1).getContent());
        assertEquals(notificationPage.getContent().get(2).getContent(),
            result.getContent().get(2).getContent());

        verify(notificationRepository, times(1)).findNotificationList(eq(seller.getId()),
            any(LocalDateTime.class), eq(pageable));
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 실패 (존재하지 않는 회원)")
    void getNotificationListFail_1() {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        given(memberRepository.findById(seller.getId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> notificationService.getNotificationList(seller.getId(), pageable));

        //then
        verify(memberRepository, times(1)).findById(seller.getId());
        verify(notificationRepository, times(0)).findNotificationList(eq(seller.getId()),
            any(LocalDateTime.class), eq(pageable));
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 실패 (findNotificationList 예외 발생)")
    void getNotificationListFail_2() {
        //given
        LocalDateTime beforeOneMonth = LocalDateTime.now().minusMonths(1);
        Pageable pageable = PageRequest.of(0, 10);

        given(memberRepository.findById(seller.getId())).willReturn(Optional.of(seller));
        given(notificationRepository.findNotificationList(seller.getId(), beforeOneMonth, pageable))
            .willThrow(new RuntimeException());

        ArgumentCaptor<LocalDateTime> dateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        //when
        assertThrows(RuntimeException.class,
            () -> notificationService.getNotificationList(seller.getId(), pageable));

        //then
        verify(memberRepository, times(1)).findById(seller.getId());
        verify(notificationRepository, times(1)).findNotificationList(eq(seller.getId()),
            dateTimeCaptor.capture(), eq(pageable));
    }
}