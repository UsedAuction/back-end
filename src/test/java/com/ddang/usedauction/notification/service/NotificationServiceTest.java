package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
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
    private Member member;
    private Auction auction;

    @BeforeEach
    void before() {

        lastEventId = "123";

        member = Member.builder()
            .id(1L)
            .memberId("member")
            .build();

        auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(member)
            .bidList(null)
            .build();
    }

    @Test
    @DisplayName("알림 구독 - 성공")
    void subscribeSuccess() {
        //given
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(emitterRepository.save(
            argThat(arg -> arg.startsWith(member.getId() + "_")),
            argThat(arg -> arg != null))
        ).willReturn(sseEmitter);

        //when
        SseEmitter resultEmitter = notificationService.subscribe(member.getEmail(), lastEventId);

        //then
        assertNotNull(resultEmitter);
        assertEquals(sseEmitter, resultEmitter);
        verify(emitterRepository).save(
            argThat(arg -> arg.startsWith(member.getId() + "_")),
            argThat(arg -> arg != null)
        );
    }

    @Test
    @DisplayName("알림 구독 - 실패 (emitterRepository 저장 실패)")
    void subscribeFail_emitterRepositorySave() {
        //given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(emitterRepository.save(
            argThat(arg -> arg.startsWith(member.getId() + "_")),
            argThat(arg -> arg != null))
        ).willThrow(new RuntimeException());

        //when
        //then
        assertThrows(RuntimeException.class,
            () -> notificationService.subscribe(member.getEmail(), lastEventId));
    }

    @Test
    @DisplayName("알림 구독 - 실패 (sendNotification 에서 전송 실패)")
    void subscribeFail_sendNotification() throws Exception {
        //given
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(emitterRepository.save(
            argThat(arg -> arg.startsWith(member.getId() + "_")),
            argThat(arg -> arg != null))
        ).willReturn(sseEmitter);

        doThrow(new IOException()).when(sseEmitter).send(any(SseEmitter.SseEventBuilder.class));

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);

        // when
        notificationService.subscribe(member.getEmail(), lastEventId);

        // then
        verify(emitterRepository, times(1)).deleteById(emitterIdCaptor.capture());
    }

    @Test
    @DisplayName("알림 구독 - 실패 (findAllEventCacheStartWithMemberId 조회 실패)")
    void subscribeFail_findAllEventCacheStartWithMemberId() {
        //given
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(emitterRepository.save(
            argThat(arg -> arg.startsWith(member.getId() + "_")),
            argThat(arg -> arg != null))
        ).willReturn(sseEmitter);
        given(emitterRepository.findAllEventCacheStartWithMemberId(String.valueOf(member.getId())))
            .willThrow(new RuntimeException("findAllEventCacheStartWithMemberId 실패"));

        //when
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> notificationService.subscribe(member.getEmail(), lastEventId));

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
            .member(member)
            .build();

        Map<String, SseEmitter> emitters = new HashMap<>();
        SseEmitter sseEmitter1 = new SseEmitter();
        SseEmitter sseEmitter2 = new SseEmitter();
        emitters.put("1_1", sseEmitter1);
        emitters.put("1_2", sseEmitter2);

        given(notificationRepository.save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member))))
            .willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId(String.valueOf(member.getId()))).willReturn(emitters);
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));

        //when
        notificationService.send(member.getId(), auction.getId(), content, notificationType);

        //then
        verify(notificationRepository, times(1)).save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member)));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId(String.valueOf(member.getId()));
        verify(emitterRepository, times(1)).saveEventCache(eq("1_1"), eq(notification));
        verify(emitterRepository, times(1)).saveEventCache(eq("1_2"), eq(notification));
        verify(memberRepository, times(1)).findById(member.getId());
        verify(auctionRepository, times(1)).findById(auction.getId());
    }

    @Test
    @DisplayName("알림 전송 - 실패 (memberRepository 회원 조회 실패)")
    void sendFail_memberRepository() {
        //given
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(member)
            .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        //when
        NoSuchElementException e = assertThrows(
            NoSuchElementException.class,
            () -> notificationService.send(member.getId(), auction.getId(), content, notificationType));

        //then
        assertEquals("존재하지 않는 회원입니다.", e.getMessage());
        verify(notificationRepository, times(0)).save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member)));
        verify(emitterRepository, times(0)).findAllEmitterStartWithMemberId(String.valueOf(member.getId()));
        verify(emitterRepository, times(0)).saveEventCache(eq("1_1"), eq(notification));
        verify(emitterRepository, times(0)).saveEventCache(eq("1_2"), eq(notification));
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
            .member(member)
            .build();

        given(notificationRepository.save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member))))
            .willReturn(notification);
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(emitterRepository.findAllEmitterStartWithMemberId(
            String.valueOf(member.getId()))).willThrow(new RuntimeException("찾기 실패"));

        //when
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> notificationService.send(member.getId(), auction.getId(), content,
                notificationType));

        //then
        assertEquals("찾기 실패", e.getMessage());
        verify(notificationRepository, times(1)).save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member)));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId(String.valueOf(member.getId()));
        verify(emitterRepository, times(0)).saveEventCache(eq("1_1"), eq(notification));
        verify(emitterRepository, times(0)).saveEventCache(eq("1_2"), eq(notification));
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
            .member(member)
            .build();

        SseEmitter emitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = Collections.singletonMap("1_1234", emitter);

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(notificationRepository.save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member))))
            .willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId(String.valueOf(member.getId()))).willReturn(emitters);

        doThrow(new RuntimeException("저장 실패")).when(emitterRepository).saveEventCache(anyString(), any(Notification.class));

        //when
        assertThrows(RuntimeException.class, () -> {
            notificationService.send(member.getId(), auction.getId(), content, notificationType);
        });

        //then
        verify(notificationRepository, times(1)).save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member)));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId(String.valueOf(member.getId()));
        verify(emitterRepository, times(0)).saveEventCache(eq("1_1"), eq(notification));
        verify(emitterRepository, times(0)).saveEventCache(eq("1_2"), eq(notification));
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
            .member(member)
            .build();

        SseEmitter emitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = Collections.singletonMap("1_1234", emitter);

        given(notificationRepository.save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member))))
            .willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId(String.valueOf(member.getId()))).willReturn(emitters);
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));

        doThrow(new IOException("전송 실패")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);

        //when
        notificationService.send(member.getId(), auction.getId(), content, notificationType);

        // then
        verify(emitterRepository, times(1)).deleteById(emitterIdCaptor.capture());
        verify(notificationRepository, times(1)).save(
            argThat(arg -> arg.getContent().equals(content) &&
                arg.getNotificationType().equals(notificationType) &&
                arg.getMember().equals(member)));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId(String.valueOf(member.getId()));
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
                    .member(member)
                    .build(),
                Notification.builder()
                    .id(2L)
                    .content("알림2")
                    .notificationType(NotificationType.CONFIRM)
                    .member(member)
                    .build(),
                Notification.builder()
                    .id(3L)
                    .content("알림3")
                    .notificationType(NotificationType.QUESTION)
                    .member(member)
                    .build()),
            pageable,
            3
        );

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(notificationRepository.findNotificationList(
            eq(member.getEmail()), any(LocalDateTime.class), eq(pageable))
        ).willReturn(notificationPage);

        //when
        Page<Notification> result = notificationService.getNotificationList(
            member.getEmail(), pageable);

        //then
        assertEquals(notificationPage.getContent().size(), result.getTotalElements());
        assertEquals(notificationPage.getContent().get(0).getContent(),
            result.getContent().get(0).getContent());
        assertEquals(notificationPage.getContent().get(1).getContent(),
            result.getContent().get(1).getContent());
        assertEquals(notificationPage.getContent().get(2).getContent(),
            result.getContent().get(2).getContent());

        verify(notificationRepository, times(1)).findNotificationList(
            eq(member.getEmail()), any(LocalDateTime.class), eq(pageable));
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 실패 (존재하지 않는 회원)")
    void getNotificationListFail_1() {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.empty());

        ArgumentCaptor<LocalDateTime> dateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        //when
        assertThrows(NoSuchElementException.class,
            () -> notificationService.getNotificationList(member.getEmail(), pageable));

        //then
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        verify(notificationRepository, times(0)).findNotificationList(
            eq(member.getEmail()), dateTimeCaptor.capture(), eq(pageable));
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 실패 (findNotificationList 예외 발생)")
    void getNotificationListFail_2() {
        //given
        LocalDateTime beforeOneMonth = LocalDateTime.now().minusMonths(1);
        Pageable pageable = PageRequest.of(0, 10);

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(notificationRepository.findNotificationList(member.getEmail(), beforeOneMonth, pageable))
            .willThrow(new RuntimeException());

        ArgumentCaptor<LocalDateTime> dateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        //when
        assertThrows(RuntimeException.class,
            () -> notificationService.getNotificationList(member.getEmail(), pageable));

        //then
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        verify(notificationRepository, times(1)).findNotificationList(
            eq(member.getEmail()), dateTimeCaptor.capture(), eq(pageable));
    }
}