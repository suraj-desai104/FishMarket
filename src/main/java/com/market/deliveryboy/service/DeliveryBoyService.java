package com.market.deliveryboy.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.market.deliveryboy.dto.DeliveryVerifyPinRequest;
import com.market.deliveryboy.dto.DeliveryverifyOtp;
import com.market.deliveryboy.model.DeliveryBoy;
import com.market.deliveryboy.repository.DeliveryBoyRepository;
import com.market.order.model.Order;
import com.market.order.model.OrderItem;
import com.market.order.repository.OrderItemRepository;
import com.market.order.repository.OrderRepository;
import com.market.product.model.Product;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.market.authentication.model.Users;
import com.market.authentication.repository.UsersRepository;
import com.market.cart.model.CartItem;
import com.market.cart.repository.CartRepository;

@Service
public class DeliveryBoyService {

    private final DeliveryBoyRepository deliveryBoyRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private CartRepository cartItemRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

	 @Value("${twilio.service-sid}")
	    private String twilioServiceSid;
	 
    public DeliveryBoyService(DeliveryBoyRepository deliveryBoyRepository) {
        this.deliveryBoyRepository = deliveryBoyRepository;
    }
    
    
    public ResponseEntity<?> verifyDeliveryOtp(DeliveryverifyOtp request){
Order order =orderRepository.findById(request.getOrderId()).orElseThrow();
    	
try {
    	Users user =usersRepository.findById(order.getUserId()).orElseThrow();
    	
    	 VerificationCheck verificationCheck = VerificationCheck.creator(twilioServiceSid)
                 .setTo("+91"+user.getPhoneNumber())
                 .setCode(request.getOtp())
                 .create();
    	 
    	 if ("approved".equalsIgnoreCase(verificationCheck.getStatus())) {
    		 
    		 order.setStatus("DELIVERED");
             orderRepository.save(order);
             
             List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

             for (OrderItem orderItem : orderItems) {
                
            	 orderItem.setDelivered(true);

                 orderItemRepository.save(orderItem);
                 
             }
             
             
             Map<String, Object> response = new HashMap<>();
             response.put("message", "Order delivered successfully!");
             response.put("orderId", order.getId());
             response.put("customerPhone", user.getPhoneNumber());

             return ResponseEntity.ok(response);
    		 
    	 }else {
    		 return ResponseEntity.badRequest().body("OTP is incorrect or expired!");
    	 }
} catch (Exception e) {
    return ResponseEntity.badRequest().body("OTP verification failed: " + e.getMessage());
}
    	
    }

    // *******************************
    // Create Delivery Boy (Admin)
    // *******************************
    public DeliveryBoy registerDeliveryBoy(DeliveryBoy deliveryBoy) {

        // Aadhaar must be unique
        if (deliveryBoyRepository.findByAadhaarNumber(deliveryBoy.getAadhaarNumber()) != null) {
            throw new RuntimeException("Aadhaar already registered!");
        }

        // New drivers are unverified
        deliveryBoy.setVerified(false);
        deliveryBoy.setActive(false);

        return deliveryBoyRepository.save(deliveryBoy);
    }

    // *******************************
    // Verify Delivery Boy (Admin)
    // *******************************
    public DeliveryBoy verifyDeliveryBoy(Long id) {

        DeliveryBoy boy = deliveryBoyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        boy.setVerified(true);
        boy.setActive(true);  // activate after verification

        return deliveryBoyRepository.save(boy);
    }

    // *******************************
    // Activate / Deactivate Driver
    // *******************************
    public DeliveryBoy updateActiveStatus(Long id, boolean status) {
        DeliveryBoy boy = deliveryBoyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (!boy.isVerified()) {
            throw new RuntimeException("Driver is not verified!");
        }

        boy.setActive(status);
        return deliveryBoyRepository.save(boy);
    }

    // *******************************
    // Get Driver By User
    // *******************************
    public DeliveryBoy getByUser(Users user) {
        return deliveryBoyRepository.findByUser(user);
    }

    // *******************************
    // Get All Verified Drivers
    // *******************************
    public List<DeliveryBoy> getAllVerifiedDrivers() {
        return deliveryBoyRepository.findByIsVerifiedTrue();
    }

    // *******************************
    // Get Only Active + Verified Drivers
    // (Auto assignment)
    // *******************************
    public List<DeliveryBoy> getAvailableDrivers() {
        return deliveryBoyRepository.findByIsVerifiedTrueAndIsActiveTrue();
    }
    
    public List<DeliveryBoy> getAllDrivers() {
        return deliveryBoyRepository.findAll();
    }
    
    public DeliveryBoy getDeliveryBoyById(Long id) {
        return deliveryBoyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
    }
    
    
    
  

    public ResponseEntity<?> verifyDeliveryPin(DeliveryverifyOtp request) {

        // 1️⃣ Validate request
        if (request.getOrderId() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body("OrderId and PIN are required");
        }

        // 2️⃣ Fetch order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 3️⃣ Check delivery boy assignment
        if (!order.getDeliveryBoyId().equals(request.getDeliveryBoyId())) {
            return ResponseEntity.badRequest().body("Delivery boy not assigned to this order");
        }

        // 4️⃣ Fetch user
        Users user = usersRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 5️⃣ Check PIN
        if (user.getDeliveryPinHash() == null) {
            return ResponseEntity.badRequest().body("User has not set delivery PIN");
        }

        if (!passwordEncoder.matches(request.getOtp(), user.getDeliveryPinHash())) {
            return ResponseEntity.badRequest().body("Invalid delivery PIN");
        }

        // 6️⃣ Update order status
        order.setStatus("DELIVERED");
        orderRepository.save(order);

        // 7️⃣ Update order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            item.setDelivered(true);
            orderItemRepository.save(item);
        }

        // 8️⃣ Response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order delivered successfully");
        response.put("orderId", order.getId());

        return ResponseEntity.ok(response);
    }


}
