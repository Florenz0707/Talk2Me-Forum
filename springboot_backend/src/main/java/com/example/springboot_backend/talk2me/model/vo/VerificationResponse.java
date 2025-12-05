package com.example.springboot_backend.talk2me.model.vo;

public class VerificationResponse {
    private String message;

    public VerificationResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
