package com.gilede.livraria.service;

import com.gilede.livraria.config.JwtService;
import com.gilede.livraria.dto.AuthDTOs;
import com.gilede.livraria.model.Role;
import com.gilede.livraria.model.User;
import com.gilede.livraria.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Email ou senha incorretos"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Email ou senha incorretos");
        }

        String token = jwtService.generateToken(user);

        return new AuthDTOs.LoginResponse(token, toUserResponse(user));
    }

    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email já cadastrado: " + request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);
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
