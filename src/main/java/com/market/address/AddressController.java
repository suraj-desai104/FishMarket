package com.market.address;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.market.authentication.model.Users;
import com.market.authentication.services.AuthService;
import com.market.order.model.Order;

@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "*")
public class AddressController {

    private final AddressService addressService;
    
    @Autowired
    private AuthService authService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // -------------------------------------------------------
    // 1️⃣ Add new address for a user
    // -------------------------------------------------------
    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addAddress(
            @PathVariable Long userId,
            @RequestBody Address address) {

        Address savedAddress = addressService.addAddress(userId, address);
        return ResponseEntity.ok(savedAddress);
    }

    // -------------------------------------------------------
    // 2️⃣ Get all addresses of a user
    // -------------------------------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAddresses(@PathVariable Long userId) {
        List<Address> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    // -------------------------------------------------------
    // 3️⃣ Delete address
    // -------------------------------------------------------
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        String message = addressService.deleteAddress(addressId);
        return ResponseEntity.ok(message);
    }

    // -------------------------------------------------------
    // 4️⃣ Attach selected address to order
    // -------------------------------------------------------
    @PostMapping("/attach/{orderId}/{addressId}")
    public ResponseEntity<?> attachAddress(
            @PathVariable Long orderId,
            @PathVariable Long addressId) {

        Order updatedOrder = addressService.attachAddressToOrder(orderId, addressId);
        return ResponseEntity.ok(updatedOrder);
    }
    
    @PostMapping("/assign")
    public ResponseEntity<?> assignDefaultAddress(@RequestBody AssignAddressDto dto) {
        Users updatedUser = authService.assignDefaultAddress(dto);
        return ResponseEntity.ok(updatedUser);
    }
}
