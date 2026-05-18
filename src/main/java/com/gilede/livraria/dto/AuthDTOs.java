package com.gilede.livraria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTOs {

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String password) {
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
