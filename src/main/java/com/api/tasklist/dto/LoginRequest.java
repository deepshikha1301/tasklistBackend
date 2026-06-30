package com.api.tasklist.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message="login id is required")
    private String loginId;

    @NotBlank(message="password is required")
    private String password;

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
