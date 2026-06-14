package com.gilede.livraria.service;

import com.gilede.livraria.dto.OrderDTOs;
import com.gilede.livraria.mapper.OrderMapper;
import com.gilede.livraria.repository.*;
import com.gilede.livraria.model.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final NotificationRepository notificationRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(orderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderDTOs.OrderResponse findById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Order findOrderEntityById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
        // força inicialização dos items dentro da transação
        order.getItems().size();
        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> findByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(orderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> findByAuthenticatedUser(Authentication authentication) {
        UUID userId = resolveAuthenticatedUserId(authentication);
        return findByUserId(userId);
    }

    /**
     * Cria um pedido:
     * 1. Valida estoque de cada item
     * 2. Calcula total
     * 3. Reduz estoque
     * 4. Persiste o pedido
     */
    @Transactional
    public OrderDTOs.OrderResponse create(Authentication authentication, OrderDTOs.CreateOrderRequest request) {
        Objects.requireNonNull(request, "Requisição de pedido é obrigatória");
        Objects.requireNonNull(request.items(), "Itens do pedido são obrigatórios");
        Objects.requireNonNull(request.address(), "Endereço do pedido é obrigatório");

        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        OrderDTOs.AddressRequest address = request.address();

        // Construção dos itens e validação de estoque
        for (OrderDTOs.CartItemRequest cartItem : request.items()) {
            Objects.requireNonNull(cartItem, "Item do pedido é obrigatório");
            UUID bookId = UUID.fromString(cartItem.bookId());
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + bookId));

            if (!book.getActive()) {
                throw new IllegalStateException("Livro inativo: " + book.getTitle());
            }
            if (book.getStock() < cartItem.quantity()) {
                throw new IllegalStateException(
                        "Estoque insuficiente para: " + book.getTitle() +
                                " (disponível: " + book.getStock() + ")");
            }

            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(cartItem.quantity()));
            subtotal = subtotal.add(itemTotal);

            OrderItem item = OrderItem.builder()
                    .book(book)
                    .quantity(cartItem.quantity())
                    .unitPrice(book.getPrice())
                    .build();
            items.add(item);
        }

        BigDecimal discount = request.discount() != null ? request.discount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);

        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.paymentMethod().toUpperCase());
        UUID resolvedUserId = resolveAuthenticatedUserId(authentication);

        Order order = Order.builder()
                .userId(resolvedUserId)
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerPhone(request.customerPhone())
                .addressStreet(address.street())
                .addressNumber(address.number())
                .addressComplement(address.complement())
                .addressNeighborhood(address.neighborhood())
                .addressCity(address.city())
                .addressState(address.state())
                .addressZipCode(address.zipCode())
                .paymentMethod(paymentMethod)
                .total(total)
                .discount(discount)
                .couponCode(request.couponCode())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        BigDecimal shipping = request.shippingCost() != null ? request.shippingCost() : BigDecimal.ZERO;
        order.setShippingCost(shipping);
        order.setTotal(order.getTotal().add(shipping));

        // Associa itens ao pedido
        for (OrderItem item : items) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        Order saved = orderRepository.save(order);

        // Reduz estoque APÓS salvar o pedido
        for (OrderDTOs.CartItemRequest cartItem : request.items()) {
            UUID bookId = UUID.fromString(cartItem.bookId());
            Book book = bookRepository.findById(bookId).orElseThrow();
            book.setStock(book.getStock() - cartItem.quantity());
            bookRepository.save(book);
        }

        log.info("Pedido criado: {} para {}", saved.getId(), saved.getCustomerEmail());
        return orderMapper.toResponse(saved);
    }

    private UUID resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório para criar pedido");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório para criar pedido");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não encontrado"));
        return user.getId();
    }

    /**
     * Atualiza status do pedido e gera notificação para o usuário.
     */
    @Transactional
    public OrderDTOs.OrderResponse updateStatus(UUID id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));

        OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Gera notificação se o pedido tiver um usuário vinculado
        if (order.getUserId() != null) {
            String message = buildNotificationMessage(status, id);
            Notification notification = Notification.builder()
                    .userId(order.getUserId())
                    .orderId(id)
                    .message(message)
                    .type("order_status")
                    .status(status)
                    .read(false)
                    .build();
            notificationRepository.save(notification);
            log.info("Notificação gerada para usuário {} — pedido {}", order.getUserId(), id);
        }

        return orderMapper.toResponse(saved);
    }

    private String buildNotificationMessage(OrderStatus status, UUID orderId) {
        String shortId = orderId.toString().substring(0, 8).toUpperCase();
        return switch (status) {
            case CONFIRMED -> "Seu pedido #" + shortId + " foi confirmado!";
            case SHIPPED -> "Seu pedido #" + shortId + " foi enviado para entrega.";
            case DELIVERED -> "Seu pedido #" + shortId + " foi entregue. Obrigado!";
            case CANCELLED -> "Seu pedido #" + shortId + " foi cancelado.";
            default -> "Seu pedido #" + shortId + " teve o status atualizado.";
        };
    }
}
