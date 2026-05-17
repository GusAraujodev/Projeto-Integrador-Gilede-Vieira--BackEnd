package com.gilede.livraria.repository;

import com.gilede.livraria.model.MercadoLivreConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MercadoLivreConfigRepository extends JpaRepository<MercadoLivreConfig, Long> {
    Optional<MercadoLivreConfig> findTopByOrderByIdDesc();

    Optional<MercadoLivreConfig> findBySellerId(String sellerId);
}