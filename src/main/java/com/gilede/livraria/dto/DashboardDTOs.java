package com.gilede.livraria.dto;

import java.math.BigDecimal;

public class DashboardDTOs {

        public record DashboardStatsResponse(
                        long totalBooks,
                        long activeBooks,
                        long totalOrders,
                        long pendingOrders,
                        long confirmedOrders,
                        long shippedOrders,
                        long deliveredOrders,
                        long cancelledOrders,
                        BigDecimal totalRevenue,
                        long lowStockBooks,
                        long outOfStockBooks) {
        }

        public record BestSellingBookResponse(
                        String bookId,
                        String title,
                        long totalSold) {
        }

        public record StockAlertResponse(
                        String bookId,
                        String title,
                        Integer stock,
                        String alertType // "LOW_STOCK" | "OUT_OF_STOCK"
        ) {
        }
}
