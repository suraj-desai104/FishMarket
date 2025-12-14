package com.market.cart.controller;

import com.market.cart.dto.AddToCartRequest;
import com.market.cart.dto.UpdateQuantityRequest;
import com.market.cart.model.CartItem;
import com.market.cart.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    // ➤ Get all cart items for a user
    @GetMapping("/{userId}")
    public List<CartItem> getUserCart(@PathVariable Long userId) {
        return cartService.getUserCart(userId);
    }

    // ➤ Add product to cart
    @PostMapping("/add")
    public CartItem addToCart(@RequestBody AddToCartRequest request) {
    	System.out.println(request.getUserId());
        return cartService.addToCart(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity()
        );
    }


    // ➤ Update quantity
    @PutMapping("/update")
    public CartItem updateQuantity(@RequestBody UpdateQuantityRequest UQR    ) {
        return cartService.updateQuantity(UQR.getCartItemId(), UQR.getQuantity());
    }

    // ➤ Delete item
    @DeleteMapping("/remove/{cartItemId}")
    public String removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return "Cart item removed";
    }

    // ➤ Clear user cart
    @DeleteMapping("/clear/{userId}")
    public String clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return "Cart cleared";
    }
}
