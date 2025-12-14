package com.market.deliveryboy.dto;

public class DeliveryVerifyPinRequest {

    private Long orderId;
    private Long deliveryBoyId;
    private String pin;   // 👈 PIN instead of OTP
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getDeliveryBoyId() {
		return deliveryBoyId;
	}
	public void setDeliveryBoyId(Long deliveryBoyId) {
		this.deliveryBoyId = deliveryBoyId;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}

    // getters & setters
}
