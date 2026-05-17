package com.gilede.livraria.repository;

import com.gilede.livraria.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    /** Catálogo público: somente livros ativos */
    List<Book> findByActiveTrue();

    /** Filtrar por categoria (público) */
    List<Book> findByCategoryAndActiveTrue(String category);

    /** Busca por título ou autor (público) */
    @Query("SELECT b FROM Book b WHERE b.active = true AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Book> searchActiveBooks(@Param("query") String query);

    /** Livros com estoque baixo (< 5) — para alertas do admin */
    @Query("SELECT b FROM Book b WHERE b.stock < 5 AND b.active = true")
    List<Book> findLowStockBooks();

    /** Livros esgotados */
    List<Book> findByStockAndActiveTrue(Integer stock);

    /** Para sincronização ML */
    boolean existsByMlId(String mlId);

    java.util.Optional<Book> findByMlId(String mlId);
}
