package com.market.order.repository;

import com.market.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Fetch all order items for a specific order
    List<OrderItem> findByOrderId(Long orderId);
}
