package com.ddang.usedauction.order.repository;

import com.ddang.usedauction.order.domain.Orders;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByMemberId(Long memberId);
}
