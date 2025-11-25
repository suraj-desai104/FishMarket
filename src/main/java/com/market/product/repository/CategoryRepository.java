package com.market.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.product.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ✅ Find category by slug
    Optional<Category> findBySlug(String slug);

    // ✅ Check if slug already exists (used while saving to avoid duplicates)
    boolean existsBySlug(String slug);

    // ✅ Optional: Check if name already exists
    boolean existsByName(String name);
}
