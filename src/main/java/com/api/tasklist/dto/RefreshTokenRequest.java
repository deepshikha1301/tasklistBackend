package com.api.tasklist.dto;

public class RefreshTokenRequest {

    private String refreshTokenId;

    public RefreshTokenRequest(String refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }

    public String getRefreshTokenId() {
        return refreshTokenId;
    }

    public void setRefreshTokenId(String refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }
}
