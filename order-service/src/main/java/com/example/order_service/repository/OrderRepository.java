package com.example.order_service.repository;

import com.example.order_service.model.Order;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    List<Order> findBySellerId(String sellerId);

    @Query("SELECT o FROM Order o WHERE o.orderDateTime >= :startDate AND o.orderDateTime <= :endDate AND o.orderStatus = 'SUCCESS'")
    List<Order> findCompletedOrdersInRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}
