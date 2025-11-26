package com.market.order.service;

import com.market.authentication.model.Users;
import com.market.authentication.repository.UsersRepository;
import com.market.cart.model.CartItem;
import com.market.cart.repository.CartRepository;
import com.market.order.model.Order;
import com.market.order.model.OrderItem;
import com.market.order.repository.OrderItemRepository;
import com.market.order.repository.OrderRepository;
import com.market.product.model.Product;
import com.market.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;
    
	@Autowired
    private UsersRepository usersRepository; 
	
    public Double getTotalSales() {
        return orderRepository.getTotalSales();
    }

    /**
     * Place order: COD or Online
     */
    @Transactional
    public Order placeOrder(Order order) {

        // 1️⃣ Fetch CartItems for user
        List<CartItem> cartItems = cartItemRepository.findByUserId(order.getUserId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // 2️⃣ Check stock & availability for each product
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (!product.isAvailable()) {
                throw new RuntimeException("Product not available: " + product.getName());
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        }

        // 3️⃣ Save Order
        Order savedOrder = orderRepository.save(order);

        // 4️⃣ Create OrderItems and update stock
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            if(product.getDiscountedPrice() != null) {
            orderItem.setPrice(product.getDiscountedPrice());
            }else {
            	orderItem.setPrice(product.getPrice());
            }
            orderItems.add(orderItem);

            // Update stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 5️⃣ Save all OrderItems
        orderItemRepository.saveAll(orderItems);

        savedOrder.setItems(orderItems);
        // 6️⃣ Clear Cart
        cartItemRepository.deleteAll(cartItems);

        // 7️⃣ Notify delivery boys (stub)
        notifyDeliveryBoys(savedOrder);

        return savedOrder;
    }
    
    public BigDecimal calculateTotalAmount(Long userId) {
        // Fetch all cart items for the user
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        // Calculate total: sum of (discountedPrice or price) * quantity
        return cartItems.stream()
                .map(c -> {
                    Product product = c.getProduct();
                    BigDecimal priceToUse = (product.getDiscountedPrice() != null) 
                            ? product.getDiscountedPrice() 
                            : product.getPrice();
                    return priceToUse.multiply(BigDecimal.valueOf(c.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Stub: Notify delivery boys about the new order
     */
    private void notifyDeliveryBoys(Order order) {
        // TODO: implement route-based delivery boy notification
//        System.out.println("Notifying eligible delivery boys for order id: " + order.getId());
    }

    // Fetch orders by user
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // Fetch orders assigned to a delivery boy
    public List<Order> getOrdersForDeliveryBoy(Long deliveryBoyId) {
        return orderRepository.findPendingOrdersByDeliveryBoy(deliveryBoyId);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
        
    }
    
    
 // Fetch all available orders for delivery boys
    public List<Order> getAvailableOrders() {
        return orderRepository.findByDeliveryBoyAssignedFalse();
    }
    
    
    @Transactional
    public Order assignOrderToDeliveryBoy(Long orderId, Long deliveryBoyId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.isDeliveryBoyAssigned()) {
            throw new RuntimeException("Order is already assigned");
        }

        order.setDeliveryBoyAssigned(true);
        order.setDeliveryBoyId(deliveryBoyId);
        return orderRepository.save(order);
    }
    
   

}
