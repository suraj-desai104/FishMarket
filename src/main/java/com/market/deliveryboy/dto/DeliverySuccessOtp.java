package com.market.deliveryboy.dto;

public class DeliverySuccessOtp {
	
	private Long orderId;
	private Long deliveryBoyId;
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
