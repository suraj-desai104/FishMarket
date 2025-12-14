package com.market.deliveryboy.controller;

import com.market.authentication.dto.UserRegistrationRequest;
import com.market.authentication.enums.Role;
import com.market.authentication.model.Users;
import com.market.authentication.repository.UsersRepository;
import com.market.authentication.services.AuthService;
import com.market.deliveryboy.dto.DeliveryBoyCreateRequestDTO;
import com.market.deliveryboy.dto.DeliverySuccessOtp;
import com.market.deliveryboy.dto.DeliveryverifyOtp;
import com.market.deliveryboy.model.DeliveryBoy;
import com.market.deliveryboy.service.DeliveryBoyService;
import com.market.order.dto.AssignOrderdto;
import com.market.order.model.Order;
import com.market.order.repository.OrderRepository;
import com.market.order.service.OrderService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/delivery-boy")
public class DeliveryBoyController {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DeliveryBoyService deliveryBoyService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    
    @Autowired
    private UsersRepository usersRepository;

    // **********************************************************************
    // Add new delivery boy (Admin only)
    // **********************************************************************
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addDeliveryBoy(@RequestBody DeliveryBoyCreateRequestDTO dto) {

        // Duplicate checks
        if (userRepository.existsByUsername(dto.getUsername()))
            throw new RuntimeException("Username already exists");

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber()))
            throw new RuntimeException("Phone number already exists");

        if (userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already exists");

        // Create User
        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.DELIVERYBOY);

        Users savedUser = userRepository.save(user);

        if (savedUser.getUserId() == null) {
            throw new RuntimeException("Failed to save user. Rolling back...");
        }

        // Create DeliveryBoy
        DeliveryBoy deliveryBoy = new DeliveryBoy();
        deliveryBoy.setUser(savedUser);
        deliveryBoy.setAadhaarNumber(dto.getAadhaarNumber());
        deliveryBoy.setVehicleNumber(dto.getVehicleNumber());

        DeliveryBoy savedDeliveryBoy = deliveryBoyService.registerDeliveryBoy(deliveryBoy);

        if (savedDeliveryBoy.getId() == null) {
            throw new RuntimeException("Failed to save delivery boy. Rolling back...");
        }

        return ResponseEntity.ok(Map.of(
                "message", "Delivery boy added successfully",
                "deliveryBoyId", savedDeliveryBoy.getId()
        ));
    }



    // **********************************************************************
    // Verify Delivery Boy (Admin)
    // **********************************************************************
    @PutMapping("/verify/{id}")
    public ResponseEntity<?> verifyDeliveryBoy(@PathVariable Long id) {
        DeliveryBoy updated = deliveryBoyService.verifyDeliveryBoy(id);

        return ResponseEntity.ok(Map.of(
                "message", "Delivery boy verified successfully",
                "deliveryBoy", updated
        ));
    }


    // **********************************************************************
    // Activate/Deactivate Driver (Admin)
    // **********************************************************************
    @PutMapping("/active-status/{id}")
    public ResponseEntity<?> updateActiveStatus(@PathVariable Long id,
                                                @RequestParam boolean status) {

        DeliveryBoy updated = deliveryBoyService.updateActiveStatus(id, status);

        return ResponseEntity.ok(Map.of(
                "message", status ? "Driver activated" : "Driver deactivated",
                "deliveryBoy", updated
        ));
    }


    // **********************************************************************
    // Get Driver by ID
    // **********************************************************************
    @GetMapping("/{id}")
    public ResponseEntity<?> getDriverById(@PathVariable Long id) {

        DeliveryBoy boy = deliveryBoyService.getDeliveryBoyById(id);

        return ResponseEntity.ok(boy);
    }


    // **********************************************************************
    // Get All Drivers
    // **********************************************************************
    @GetMapping("/all")
    public ResponseEntity<?> getAllDrivers() {
        List<DeliveryBoy> drivers = deliveryBoyService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }


    // **********************************************************************
    // Get All Verified Drivers
    // **********************************************************************
    @GetMapping("/verified")
    public ResponseEntity<?> getVerifiedDrivers() {
        List<DeliveryBoy> drivers = deliveryBoyService.getAllVerifiedDrivers();
        return ResponseEntity.ok(drivers);
    }


    // **********************************************************************
    // Get All Active & Verified Drivers (Auto assignment)
    // **********************************************************************
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDrivers() {
        List<DeliveryBoy> drivers = deliveryBoyService.getAvailableDrivers();
        return ResponseEntity.ok(drivers);
    }
    
    @PutMapping("/assign-order")
    public Order assignOrderToDeliveryBoy (@RequestBody AssignOrderdto AO){
    	
    	return orderService.assignOrderToDeliveryBoy(AO.getOrderId(), AO.getDeliveryBoyId());
    	
    }

    @PostMapping("/send-otp-confirm")
    public ResponseEntity<?> registerUser(@RequestBody DeliverySuccessOtp request) {
    	
    	 if (request.getOrderId() == null || request.getDeliveryBoyId() == null) {
    	        return ResponseEntity.badRequest().body("orderId and deliveryBoyId are required!");
    	    }

    	    Order order = orderRepository.findById(request.getOrderId())
    	            .orElseThrow(() -> new RuntimeException("Order not found"));

    	    // (Optional) Check delivery boy assigned to this order
    	    if (!order.getDeliveryBoyId().equals(request.getDeliveryBoyId())) {
    	        return ResponseEntity.badRequest().body("This delivery boy is not assigned to this order!");
    	    }

    	    Users user = usersRepository.findById(order.getUserId())
    	            .orElseThrow(() -> new RuntimeException("User not found"));
        return authService.sendOtp(user.getPhoneNumber());
    }
    
    @PostMapping("/order/deliver")
    public ResponseEntity<?> verifyDeliveryOtp(@RequestBody DeliveryverifyOtp request){
    	if (request.getOrderId() == null || request.getDeliveryBoyId() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body("orderId, deliveryBoyId and otp are required!");
        }
//    	return deliveryBoyService.verifyDeliveryOtp(request);
    	return deliveryBoyService.verifyDeliveryPin(request);
    }
}
