package com.market.authentication.dto;

public class LoginRequest {
	
	   private String usernameOrPhone;
	   private String password;
	public LoginRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public LoginRequest(String usernameOrPhone, String password) {
		super();
		this.usernameOrPhone = usernameOrPhone;
		this.password = password;
	}
	public String getUsernameOrPhone() {
		return usernameOrPhone;
	}
	public void setUsernameOrPhone(String usernameOrPhone) {
		this.usernameOrPhone = usernameOrPhone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	   
	   


}
