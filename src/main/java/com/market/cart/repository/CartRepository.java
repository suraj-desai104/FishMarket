package com.market.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.market.cart.model.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    // FIXED METHOD
    Optional<CartItem> findByUserIdAndProduct_Id(Long userId, Long productId);

    void deleteByUserId(Long userId);
}
