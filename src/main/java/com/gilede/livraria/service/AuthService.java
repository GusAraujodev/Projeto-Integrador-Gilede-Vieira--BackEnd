package com.gilede.livraria.service;

import com.gilede.livraria.config.JwtService;
import com.gilede.livraria.dto.AuthDTOs;
import com.gilede.livraria.repository.UserRepository;
import com.gilede.livraria.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request) {
        // Lança exceção automaticamente se credenciais forem inválidas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(user);

        return new AuthDTOs.LoginResponse(token, toUserResponse(user));
    }

    public AuthDTOs.UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return toUserResponse(user);
    }

    private AuthDTOs.UserResponse toUserResponse(User user) {
        // role em lowercase para compatibilidade com o frontend: "admin" | "customer"
        String roleForFrontend = user.getRole().name().toLowerCase();
        return new AuthDTOs.UserResponse(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                roleForFrontend);
    }
}
