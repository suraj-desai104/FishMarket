package com.market.order.payment;




import com.market.order.model.OrderEntity;
import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // react dev server
public class PaymentController {

    private final RazorpayService razorpayService;
    private final OrderRepository1 orderRepository;

    @Value("${razorpay.key}")
    private String razorpayKey;

    public PaymentController(RazorpayService razorpayService, OrderRepository1 orderRepository) {
        this.razorpayService = razorpayService;
        this.orderRepository = orderRepository;
    }

    // Create order endpoint
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {

        try {
            // FIX: Convert amount safely
            int amount = Integer.parseInt(data.get("amount").toString());

            // Create order
            Order order = razorpayService.createOrder(amount);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("razorpayKey", razorpayKey);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



    // Verify payment
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        try {
            String razorpayOrderId = payload.get("razorpay_order_id");
            String razorpayPaymentId = payload.get("razorpay_payment_id");
            String razorpaySignature = payload.get("razorpay_signature");

            boolean isValid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);
            if (!isValid) {
                return ResponseEntity.status(400).body(Map.of("status", "failure", "reason", "Invalid signature"));
            }

            // mark order paid
            OrderEntity oe = orderRepository.findByRazorpayOrderId(razorpayOrderId);
            if (oe != null) {
                oe.setRazorpayPaymentId(razorpayPaymentId);
                oe.setRazorpaySignature(razorpaySignature);
                oe.setStatus("PAID");
                orderRepository.save(oe);
            }

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
