package com.market.address;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Get all addresses of a specific user
    List<Address> findByUserUserId(Long userId);

    // Optional: get default address if you later add a "isDefault" field
    // Address findByUserUserIdAndIsDefaultTrue(Long userId);
}
