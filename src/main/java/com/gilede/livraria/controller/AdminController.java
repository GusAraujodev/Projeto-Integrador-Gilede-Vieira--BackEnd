package com.gilede.livraria.controller;

import com.gilede.livraria.dto.DashboardDTOs;
import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.dto.OrderDTOs;
import com.gilede.livraria.service.BookService;
import com.gilede.livraria.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final BookService bookService;

    /** GET /admin/dashboard/stats */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardDTOs.DashboardStatsResponse> stats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /** GET /admin/dashboard/recent-orders */
    @GetMapping("/dashboard/recent-orders")
    public ResponseEntity<List<OrderDTOs.OrderResponse>> recentOrders() {
        return ResponseEntity.ok(dashboardService.getRecentOrders());
    }

    /** GET /admin/dashboard/best-selling-books */
    @GetMapping("/dashboard/best-selling-books")
    public ResponseEntity<List<DashboardDTOs.BestSellingBookResponse>> bestSelling() {
        return ResponseEntity.ok(dashboardService.getBestSellingBooks());
    }

    /** GET /admin/dashboard/alerts */
    @GetMapping("/dashboard/alerts")
    public ResponseEntity<List<DashboardDTOs.StockAlertResponse>> alerts() {
        return ResponseEntity.ok(dashboardService.getStockAlerts());
    }

    /**
     * GET /admin/books
     * Lista TODOS os livros (ativos e inativos) — visão administrativa.
     */
    @GetMapping("/books")
    public ResponseEntity<List<BookDTOs.BookResponse>> allBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }
}
