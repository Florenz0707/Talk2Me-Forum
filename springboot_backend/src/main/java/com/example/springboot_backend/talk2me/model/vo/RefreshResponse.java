package com.example.springboot_backend.talk2me.model.vo;

public class RefreshResponse {
    private String message;

    public RefreshResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
