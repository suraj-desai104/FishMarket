package com.market.order.repository;

import com.market.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get all orders of a user
    List<Order> findByUserId(Long userId);

    // Get orders by delivery boy assignment
    List<Order> findByDeliveryBoyAssigned(boolean assigned);

    // Optional: get orders assigned to a specific delivery boy
//    List<Order> findByDeliveryBoyId(Long deliveryBoyId);
    @Query("SELECT o FROM Order o WHERE o.deliveryBoyId = :deliveryBoyId AND o.status <> 'DELIVERED'")
    List<Order> findPendingOrdersByDeliveryBoy( Long deliveryBoyId);

    
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double getTotalSales();
    
    // Get all orders where delivery boy is NOT assigned
    List<Order> findByDeliveryBoyAssignedFalse();

}
