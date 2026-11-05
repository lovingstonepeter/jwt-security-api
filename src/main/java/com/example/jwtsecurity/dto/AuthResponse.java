package com.example.jwtsecurity.dto;

public record AuthResponse(String token, String tokenType, long expiresInMs) {
}
