package com.gilede.livraria.service;

import com.gilede.livraria.config.JwtService;
import com.gilede.livraria.dto.AuthDTOs;
import com.gilede.livraria.model.Address;
import com.gilede.livraria.model.Role;
import com.gilede.livraria.model.User;
import com.gilede.livraria.model.UserProfile;
import com.gilede.livraria.repository.AddressRepository;
import com.gilede.livraria.repository.UserRepository;
import com.gilede.livraria.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
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

    @Transactional
    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest request) {
        if (request.password() == null || !request.password().equals(request.confirmPassword())) {
            throw new IllegalStateException("As senhas informadas não coincidem");
        }

        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalStateException("Senha deve ter no mínimo 8 caracteres");
        }

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

    @Transactional(readOnly = true)
    public AuthDTOs.UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthDTOs.UserResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + email));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    private AuthDTOs.UserResponse toUserResponse(User user) {
        // role em lowercase para compatibilidade com o frontend: "admin" | "customer"
        String roleForFrontend = user.getRole().name().toLowerCase();
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        Address latestAddress = addressRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
        return new AuthDTOs.UserResponse(
                user.getId().toString(),
                resolveName(user, profile),
                user.getEmail(),
                resolvePhone(user, profile),
                resolveZipCode(user, latestAddress),
                resolveStreet(user, latestAddress),
                resolveNumber(user, latestAddress),
                resolveComplement(user, latestAddress),
                resolveNeighborhood(user, latestAddress),
                resolveCity(user, latestAddress),
                resolveState(user, latestAddress),
                roleForFrontend);
    }

    @Transactional
    public AuthDTOs.UserResponse updateContact(String email, AuthDTOs.UpdateContactRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("Usuário não encontrado: " + email));

        UserProfile profile = userProfileRepository.findByUser(user).orElseGet(() -> UserProfile.builder()
                .user(user)
                .build());

        profile.setName(request.name().trim());

        if (request.phone() != null) {
            profile.setPhone(request.phone().trim().isEmpty() ? null : request.phone().trim());
        }

        if (request.notificationEmail() != null) {
            profile.setNotificationEmail(request.notificationEmail().trim().isEmpty()
                    ? null
                    : request.notificationEmail().trim());
        } else if (profile.getNotificationEmail() == null || profile.getNotificationEmail().isBlank()) {
            profile.setNotificationEmail(user.getEmail());
        }

        userProfileRepository.save(profile);
        return toUserResponse(user);
    }

    @Transactional
    public AuthDTOs.UserResponse updateAddress(String email, AuthDTOs.UpdateAddressRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("Usuário não encontrado: " + email));

        Address address = Address.builder()
                .user(user)
                .zipCode(request.zipCode())
                .street(request.street())
                .number(request.number())
                .complement(request.complement())
                .neighborhood(request.neighborhood())
                .city(request.city())
                .state(request.state())
                .isDefault(true)
                .build();

        addressRepository.save(address);
        return toUserResponse(user);
    }

    private String resolveName(User user, UserProfile profile) {
        if (profile != null && profile.getName() != null && !profile.getName().isBlank()) {
            return profile.getName();
        }
        return user.getName();
    }

    private String resolvePhone(User user, UserProfile profile) {
        if (profile != null && profile.getPhone() != null && !profile.getPhone().isBlank()) {
            return profile.getPhone();
        }
        return user.getPhone();
    }

    private String resolveZipCode(User user, Address address) {
        if (address != null && address.getZipCode() != null && !address.getZipCode().isBlank()) {
            return address.getZipCode();
        }
        return user.getZipCode();
    }

    private String resolveStreet(User user, Address address) {
        if (address != null && address.getStreet() != null && !address.getStreet().isBlank()) {
            return address.getStreet();
        }
        return user.getStreet();
    }

    private String resolveNumber(User user, Address address) {
        if (address != null && address.getNumber() != null && !address.getNumber().isBlank()) {
            return address.getNumber();
        }
        return user.getNumber();
    }

    private String resolveComplement(User user, Address address) {
        if (address != null && address.getComplement() != null && !address.getComplement().isBlank()) {
            return address.getComplement();
        }
        return user.getComplement();
    }

    private String resolveNeighborhood(User user, Address address) {
        if (address != null && address.getNeighborhood() != null && !address.getNeighborhood().isBlank()) {
            return address.getNeighborhood();
        }
        return user.getNeighborhood();
    }

    private String resolveCity(User user, Address address) {
        if (address != null && address.getCity() != null && !address.getCity().isBlank()) {
            return address.getCity();
        }
        return user.getCity();
    }

    private String resolveState(User user, Address address) {
        if (address != null && address.getState() != null && !address.getState().isBlank()) {
            return address.getState();
        }
        return user.getState();
    }
}
