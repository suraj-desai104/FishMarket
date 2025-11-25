package com.market.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.product.model.Product;
import com.market.product.model.Category;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ Check duplicate name
    boolean existsByName(String name);

    // ✅ Check duplicate slug
    boolean existsBySlug(String slug);

    // ✅ Find product by slug
    Optional<Product> findBySlug(String slug);

    // ✅ Find all products by category
    List<Product> findByCategory(Category category);

    // ✅ Optional: find active/available products only
    List<Product> findByAvailableTrue();
}
