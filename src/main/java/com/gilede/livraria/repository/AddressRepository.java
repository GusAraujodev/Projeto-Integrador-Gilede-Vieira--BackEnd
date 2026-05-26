package com.gilede.livraria.repository;

import com.gilede.livraria.model.Address;
import com.gilede.livraria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    Optional<Address> findTopByUserOrderByCreatedAtDesc(User user);

    List<Address> findByUserOrderByCreatedAtDesc(User user);
}