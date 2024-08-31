package com.ddang.usedauction.notification.repository;

import com.ddang.usedauction.notification.domain.Notification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from notification n "
        + "where n.member.memberId = :memberId "
        + "and n.createdAt >= :beforeOneMonth "
        + "order by n.createdAt desc")
    Page<Notification> findNotificationList(
        @Param("memberId") String memberId,
        @Param("beforeOneMonth") LocalDateTime beforeOneMonth,
        Pageable pageable);
}
