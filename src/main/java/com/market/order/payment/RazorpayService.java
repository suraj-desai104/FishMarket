package com.market.order.payment;


import com.market.order.model.OrderEntity;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class RazorpayService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    private final OrderRepository1 orderRepository;

    public RazorpayService(OrderRepository1 orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    
    public Order createOrder(int amountInRupees) throws Exception {

        // Initialize client
        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

        // Convert Rupees → Paise
        int amountInPaise = amountInRupees * 100;

        // Order Request Object
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");

        // Short 8 char receipt ID (under 40 chars)
        String receipt = "rcpt_" + UUID.randomUUID().toString().substring(0, 8);
        orderRequest.put("receipt", receipt);

        // Auto capture payment
        orderRequest.put("payment_capture", 1);

        // Create order in Razorpay
        Order order = client.orders.create(orderRequest);

        // Save in DB
        OrderEntity oe = new OrderEntity();
        oe.setRazorpayOrderId(order.get("id"));
        oe.setAmount(amountInRupees);
        oe.setStatus("CREATED");

        orderRepository.save(oe);

        return order;
    }

    // Verify signature: signature == HMAC_SHA256(order_id + "|" + payment_id, secret)
    public boolean verifySignature(String orderId, String paymentId, String signature) throws Exception {
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes());
        String computed = bytesToHex(digest);
        // Razorpay sends hex lowercase signature
        return computed.equals(signature);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b: bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
