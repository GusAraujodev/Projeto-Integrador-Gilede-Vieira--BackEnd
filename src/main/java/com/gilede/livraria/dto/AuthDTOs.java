package com.gilede.livraria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDTOs {

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record LoginResponse(
            String token,
            UserResponse user) {
    }

    public record UserResponse(
            String id,
            String name,
            String email,
            String role // "admin" | "customer" — exato do frontend
    ) {
    }
}
