package com.market.order.dto;

public class OrderDto {

    private Long userId;                   // ID of the user placing the order
    private String paymentMethod;          // COD, ONLINE
    private boolean deliveryBoyAssigned = false;  // false until assigned
    private Long addressId;                // ID of the delivery address

    // ---------------- Getters & Setters ----------------

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isDeliveryBoyAssigned() {
        return deliveryBoyAssigned;
    }

    public void setDeliveryBoyAssigned(boolean deliveryBoyAssigned) {
        this.deliveryBoyAssigned = deliveryBoyAssigned;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
