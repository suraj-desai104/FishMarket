package com.market.cart.service;

import com.market.cart.model.CartItem;
import com.market.cart.repository.CartRepository;
import com.market.product.model.Product;
import com.market.product.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // ➤ Get all cart items for a user
    public List<CartItem> getUserCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    // ➤ Add product to cart
    public CartItem addToCart(Long userId, Long productId, int quantity) {

        // Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate stock
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        // Check if already in cart → FIXED METHOD NAME
        CartItem existingItem = cartRepository
                .findByUserIdAndProduct_Id(userId, productId)
                .orElse(null);

        if (existingItem != null) {

            int newQuantity = existingItem.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Reached maximum stock limit");
            }

            existingItem.setQuantity(newQuantity);
            return cartRepository.save(existingItem);
        }

        // Create new cart entry
        CartItem newItem = new CartItem();
        newItem.setUserId(userId);
        newItem.setProduct(product);
        newItem.setQuantity(quantity);

        return cartRepository.save(newItem);
    }

    // ➤ Update quantity
    public CartItem updateQuantity(Long cartItemId, int quantity) {

        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Product product = item.getProduct();

        // Validate stock
        if (quantity > product.getStockQuantity()) {
            throw new RuntimeException("Stock not available");
        }

        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    // ➤ Remove an item
    public void removeCartItem(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }

    // ➤ Clear all items of user
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
}
