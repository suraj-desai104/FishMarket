package com.market.deliveryboy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.deliveryboy.model.DeliveryBoy;
import com.market.authentication.model.Users;

@Repository
public interface DeliveryBoyRepository extends JpaRepository<DeliveryBoy, Long> {

    // Find delivery boy by linked user
    DeliveryBoy findByUser(Users user);

    // Find delivery boy by Aadhaar
    DeliveryBoy findByAadhaarNumber(String aadhaarNumber);

    // Get only verified delivery boys
    List<DeliveryBoy> findByIsVerifiedTrue();

    // Get active + verified delivery boys (ready to take orders)
    List<DeliveryBoy> findByIsVerifiedTrueAndIsActiveTrue();
}
