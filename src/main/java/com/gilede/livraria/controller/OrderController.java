package com.gilede.livraria.controller;

import com.gilede.livraria.dto.OrderDTOs;
import com.gilede.livraria.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** GET /orders — Admin: todos os pedidos */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTOs.OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    /** GET /orders/{id} — Autenticado */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTOs.OrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    /** GET /orders/user/{userId} — Histórico do cliente */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTOs.OrderResponse>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    /** POST /orders — Criar pedido (autenticado ou guest) */
    @PostMapping
    public ResponseEntity<OrderDTOs.OrderResponse> create(
            @Valid @RequestBody OrderDTOs.CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    /** PATCH /orders/{id}/status — Admin: alterar status e gerar notificação */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTOs.OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderDTOs.UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
}
