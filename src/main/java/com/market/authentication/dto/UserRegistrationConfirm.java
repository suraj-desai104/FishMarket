package com.market.authentication.dto;

public class UserRegistrationConfirm {
	
	    private String username;
	    private String fullName;
	    private String phoneNumber;
	    private String email;  // Optional
	    private String password;
	    private String otp;
		public UserRegistrationConfirm() {
			super();
			// TODO Auto-generated constructor stub
		}
		public UserRegistrationConfirm(String username, String fullName, String phoneNumber, String email,
				String password, String otp) {
			super();
			this.username = username;
			this.fullName = fullName;
			this.phoneNumber = phoneNumber;
			this.email = email;
			this.password = password;
			this.otp = otp;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getFullName() {
			return fullName;
		}
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		public String getPhoneNumber() {
			return phoneNumber;
		}
		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public String getOtp() {
			return otp;
		}
		public void setOtp(String otp) {
			this.otp = otp;
		}
	    
	    
	    

}
