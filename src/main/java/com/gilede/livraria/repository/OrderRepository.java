package com.gilede.livraria.repository;

import com.gilede.livraria.model.Order;
import com.gilede.livraria.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    /** 5 pedidos mais recentes para o dashboard */
    List<Order> findTop5ByOrderByCreatedAtDesc();

    /** Receita total excluindo cancelados */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status <> 'CANCELLED'")
    java.math.BigDecimal calculateTotalRevenue();

    /** Contagem por status */
    long countByStatus(OrderStatus status);

    /** Para o gráfico de livros mais vendidos */
    @Query("""
            SELECT oi.book.id, oi.book.title, SUM(oi.quantity) as totalSold
            FROM OrderItem oi
            WHERE oi.order.status <> 'CANCELLED'
            GROUP BY oi.book.id, oi.book.title
            ORDER BY totalSold DESC
            """)
    List<Object[]> findBestSellingBooks(org.springframework.data.domain.Pageable pageable);
}
