package com.market.deliveryboy.dto;

public class DeliveryverifyOtp {

	private Long orderId;
	private Long deliveryBoyId;
	private String otp;
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
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
	
}
