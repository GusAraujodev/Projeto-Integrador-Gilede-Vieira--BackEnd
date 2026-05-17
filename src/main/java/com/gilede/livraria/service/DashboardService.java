package com.gilede.livraria.service;

import com.gilede.livraria.dto.DashboardDTOs;
import com.gilede.livraria.dto.OrderDTOs;
import com.gilede.livraria.mapper.OrderMapper;
import com.gilede.livraria.repository.BookRepository;
import com.gilede.livraria.repository.OrderRepository;
import com.gilede.livraria.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public DashboardDTOs.DashboardStatsResponse getStats() {
        long totalBooks = bookRepository.count();
        long activeBooks = bookRepository.findByActiveTrue().size();
        long totalOrders = orderRepository.count();
        long pending = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmed = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shipped = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);
        var totalRevenue = orderRepository.calculateTotalRevenue();
        long lowStock = bookRepository.findLowStockBooks().size();
        long outOfStock = bookRepository.findByStockAndActiveTrue(0).size();

        return new DashboardDTOs.DashboardStatsResponse(
                totalBooks, activeBooks,
                totalOrders, pending, confirmed, shipped, delivered, cancelled,
                totalRevenue,
                lowStock, outOfStock);
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> getRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(orderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardDTOs.BestSellingBookResponse> getBestSellingBooks() {
        List<Object[]> raw = orderRepository.findBestSellingBooks(PageRequest.of(0, 10));
        return raw.stream().map(row -> new DashboardDTOs.BestSellingBookResponse(
                row[0].toString(),
                row[1].toString(),
                ((Number) row[2]).longValue())).toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardDTOs.StockAlertResponse> getStockAlerts() {
        List<DashboardDTOs.StockAlertResponse> alerts = new ArrayList<>();

        bookRepository.findByStockAndActiveTrue(0).forEach(b -> alerts.add(new DashboardDTOs.StockAlertResponse(
                b.getId().toString(), b.getTitle(), b.getStock(), "OUT_OF_STOCK")));

        bookRepository.findLowStockBooks().forEach(b -> {
            if (b.getStock() > 0) { // evita duplicar os zerados
                alerts.add(new DashboardDTOs.StockAlertResponse(
                        b.getId().toString(), b.getTitle(), b.getStock(), "LOW_STOCK"));
            }
        });

        return alerts;
    }
}
