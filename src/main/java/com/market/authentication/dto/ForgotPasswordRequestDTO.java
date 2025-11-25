package com.market.authentication.dto;

public class ForgotPasswordRequestDTO {
    private String usernameOrPhone;

    public String getUsernameOrPhone() {
        return usernameOrPhone;
    }

    public void setUsernameOrPhone(String usernameOrPhone) {
        this.usernameOrPhone = usernameOrPhone;
    }
}
