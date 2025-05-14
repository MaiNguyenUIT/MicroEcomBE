package com.example.order_service.utils;

import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.OrderTracker;
import com.example.order_service.repository.OrderTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SagaTracker {
    @Autowired
    private OrderTrackerRepository orderTrackerRepository;

    public void markOrderAsSuccess(String groupId, Long orderId) {
        OrderTracker orderTracker = orderTrackerRepository.findById(orderId.toString()+groupId).orElse(null);
        if (orderTracker != null){
            orderTracker.setStatus("success");
            orderTrackerRepository.save(orderTracker);
        }
         else {
             return;
        }
    }

    public void markOrderAsFail(String groupId, Long orderId) {
        OrderTracker orderTracker = new OrderTracker();
        orderTracker.setOrderId(orderId);
        orderTracker.setOrderGroupId(groupId);
        orderTracker.setStatus("fail");
        orderTrackerRepository.save(orderTracker);
    }

    public boolean isAllOrdersSuccessful(String groupId) {
        List<OrderTracker> orderTrackers = orderTrackerRepository.findByStatus("fail"+groupId);
        return orderTrackers.isEmpty();
    }

    public void cleanup(String groupId) {
        orderTrackerRepository.deleteAllByOrderGroupId(groupId);
    }
}
