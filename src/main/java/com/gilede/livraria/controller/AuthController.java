package com.gilede.livraria.controller;

import com.gilede.livraria.dto.AuthDTOs;
import com.gilede.livraria.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     * Body: { email, password }
     * Response: { token, user: { id, name, email, role } }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.LoginResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /auth/logout
     * Stateless — o token expira naturalmente.
     * O frontend é responsável por remover o token do localStorage.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /auth/me
     * Retorna dados do usuário autenticado via token JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthDTOs.UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }
}
