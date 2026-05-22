package com.gilede.livraria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mercado_livre_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MercadoLivreConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 600, nullable = false)
    private String accessToken;

    @Column(length = 200, nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 50, nullable = false)
    @Builder.Default
    private String sellerId = "532947791";

    public boolean isExpired() {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt.minusMinutes(5));
    }
}