package com.gilede.livraria.dto;

import jakarta.validation.constraints.*;

public class AuthDTOs {

        public record LoginRequest(
                        @NotBlank @Email String email,
                        @NotBlank String password) {
        }

        public record RegisterRequest(
                        @NotBlank(message = "Nome obrigatório") @Size(min = 2, max = 150) String name,
                        @NotBlank(message = "Email obrigatório") @Email String email,
                        @NotBlank(message = "Senha obrigatória") @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") String password,
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
                        @NotBlank @Size(min = 2, max = 150, message = "Nome deve ter entre 2 e 150 caracteres") String name,
                        @Size(max = 20) String phone,
                        @Email String notificationEmail) {
        }

        public record UpdateAddressRequest(
                        String zipCode, String street, String number, String complement, String neighborhood,
                        String city,
                        @Size(max = 2) String state) {
        }
}
