package com.ddang.usedauction.point.repository;

import com.ddang.usedauction.point.domain.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PointRepository extends JpaRepository<PointHistory, Long> {

    @Query("SELECT p FROM point_history p " +
        "WHERE p.member.email =:username " +
        "AND p.registerDate BETWEEN :startDate AND :endDate")
    Page<PointHistory> findAllPoint(
        @Param("username") String username,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
}
