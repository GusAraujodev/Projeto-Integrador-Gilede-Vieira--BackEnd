package com.gilede.livraria.service;

import com.gilede.livraria.dto.OrderDTOs;
import com.gilede.livraria.mapper.OrderMapper;
import com.gilede.livraria.repository.*;
import com.gilede.livraria.model.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
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
    public List<OrderDTOs.OrderResponse> findByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(orderMapper::toResponse).toList();
    }

    /**
     * Cria um pedido:
     * 1. Valida estoque de cada item
     * 2. Calcula total
     * 3. Reduz estoque
     * 4. Persiste o pedido
     */
    @Transactional
    public OrderDTOs.OrderResponse create(OrderDTOs.CreateOrderRequest request) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Construção dos itens e validação de estoque
        for (OrderDTOs.CartItemRequest cartItem : request.items()) {
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

        Order order = Order.builder()
                .userId(request.userId() != null ? UUID.fromString(request.userId()) : null)
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerPhone(request.customerPhone())
                .addressStreet(request.address().street())
                .addressNumber(request.address().number())
                .addressComplement(request.address().complement())
                .addressNeighborhood(request.address().neighborhood())
                .addressCity(request.address().city())
                .addressState(request.address().state())
                .addressZipCode(request.address().zipCode())
                .paymentMethod(paymentMethod)
                .total(total)
                .discount(discount)
                .couponCode(request.couponCode())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

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
