package com.ddang.usedauction.point.repository;

import com.ddang.usedauction.point.domain.PointHistory;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<PointHistory, Long> {

    Page<PointHistory> findByMemberEmailAndCreatedAtBetween(
        @Param("email") String email,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
