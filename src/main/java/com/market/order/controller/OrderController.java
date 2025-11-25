package com.market.order.controller;

import com.market.address.AddressRepository;
import com.market.authentication.model.Users;
import com.market.authentication.repository.UsersRepository;
import com.market.order.dto.OrderDto;
import com.market.order.model.Order;
import com.market.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private  AddressRepository addressRepository;

	@Autowired
    private UsersRepository usersRepository;

    /**
     * Place a new order (COD or Online)
     * Expects JSON:
     * {
     *   "userId": 1,
     *   "paymentMethod": "COD",
     *   "deliveryBoyAssigned": false
     * }
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderDto orderdto) {

    	System.out.println("********************");
        if (orderdto.getUserId() == null || orderdto.getPaymentMethod() == null ) {
            return ResponseEntity.badRequest().body("userId, paymentMethod and addressId are required!");
        }

        try {
            Order order = new Order();
            order.setUserId(orderdto.getUserId());
            order.setPaymentMethod(orderdto.getPaymentMethod());
            order.setDeliveryBoyAssigned(orderdto.isDeliveryBoyAssigned());

            System.out.println(orderdto.getAddressId());
            // Calculate totalAmount from cart
            BigDecimal totalAmount = orderService.calculateTotalAmount(order.getUserId());
            order.setTotalAmount(totalAmount);
            order.setStatus("PENDING");

            // Set delivery address
            Users user = usersRepository.findById(orderdto.getUserId()).orElseThrow();
            System.out.println(user.getEmail());
            order.setDeliveryAddress(usersRepository.findById(orderdto.getUserId()).get().getDefaultAddress());

            if (order.getDeliveryAddress()==null) {
            	System.out.println("+++++++++++++++++++++");
            	 return ResponseEntity.badRequest().body("Please Enter Adrees address is require !");
            }
            Order savedOrder = orderService.placeOrder(order);
            return ResponseEntity.ok(savedOrder);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Order placement failed: " + e.getMessage());
        }
    }


    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    /**
     * Get all orders for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all orders assigned to a delivery boy
     */
    @GetMapping("/delivery-boy/{deliveryBoyId}")
    public ResponseEntity<List<Order>> getOrdersForDeliveryBoy(@PathVariable Long deliveryBoyId) {
        List<Order> orders = orderService.getOrdersForDeliveryBoy(deliveryBoyId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/sales/total")
    public Double getTotalSales() {
        return orderService.getTotalSales();
    }
    
    @GetMapping("/available-orders")
    public ResponseEntity<List<Order>> getAvailableOrders() {
        List<Order> orders = orderService.getAvailableOrders();
        return ResponseEntity.ok(orders);
    }
    
    
}
