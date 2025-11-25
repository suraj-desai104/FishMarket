package com.market.cart.dto;

public class UpdateQuantityRequest {
    private Long cartItemId;
    private int quantity;
	public UpdateQuantityRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public UpdateQuantityRequest(Long cartItemId, int quantity) {
		super();
		this.cartItemId = cartItemId;
		this.quantity = quantity;
	}
	public Long getCartItemId() {
		return cartItemId;
	}
	public void setCartItemId(Long cartItemId) {
		this.cartItemId = cartItemId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
    
    
}