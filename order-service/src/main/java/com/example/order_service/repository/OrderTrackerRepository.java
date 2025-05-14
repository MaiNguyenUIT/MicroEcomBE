package com.example.order_service.repository;

import com.example.order_service.model.OrderTracker;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderTrackerRepository extends CrudRepository<OrderTracker, String> {
    List<OrderTracker> findByStatus(String groupId);
    void deleteAllByOrderGroupId(String groupId);
}
