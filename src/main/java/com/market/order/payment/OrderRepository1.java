package com.market.order.payment;


import org.springframework.data.jpa.repository.JpaRepository;

import com.market.order.model.OrderEntity;

public interface OrderRepository1 extends JpaRepository<OrderEntity, Long> {
    OrderEntity findByRazorpayOrderId(String razorpayOrderId);
}
