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
            @NotBlank @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") String password,
            @NotBlank String confirmPassword) {
    }

    public record LoginResponse(
            String token,
            UserResponse user) {
    }

    public record UserResponse(
            String id,
            String name,
            String email,
            String phone,
            String zipCode,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state,
            String role // "admin" | "customer" — exato do frontend
    ) {
    }

    public record UpdateContactRequest(
            @NotBlank(message = "Nome é obrigatório") @Size(min = 2, max = 150) String name,
            @Size(max = 20) String phone,
            @Email String notificationEmail
    ) {
    }

    public record UpdateAddressRequest(
            String zipCode, String street, String number, String complement, String neighborhood, String city,
            @Size(max = 2) String state
    ) {
    }
}
