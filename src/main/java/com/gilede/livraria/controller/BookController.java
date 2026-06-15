package com.gilede.livraria.controller;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * GET /books?search=termo
     * Público — retorna somente livros ativos.
     * Admin acessa GET /admin/books para ver todos (incluindo inativos).
     */
    @GetMapping
public ResponseEntity<List<BookDTOs.BookResponse>> getAll(
        @RequestParam(required = false) String search) {
    
    // Se tem search, usa findAllActive (com cache por search)
    // Se não tem search, usa findAll (cache geral)
    if (search != null && !search.isBlank()) {
        return ResponseEntity.ok(bookService.findAllActive(search));
    } else {
        return ResponseEntity.ok(bookService.findAll());
    }
}

    /**
     * GET /books/{id}
     * Público — retorna livro por ID (ativo ou inativo, para uso no admin também).
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDTOs.BookResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    /**
     * GET /books/category/{category}
     * Público — filtra livros ativos por categoria.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BookDTOs.BookResponse>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(bookService.findByCategory(category));
    }

    /**
     * POST /books — ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTOs.BookResponse> create(@Valid @RequestBody BookDTOs.BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    /**
     * PUT /books/{id} — ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTOs.BookResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody BookDTOs.BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    /**
     * DELETE /books/{id} — ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /books/{id}/status — ADMIN
     * Body: { active: true | false }
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTOs.BookResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BookDTOs.StatusRequest request) {
        return ResponseEntity.ok(bookService.updateStatus(id, request));
    }

    /**
     * PATCH /books/{id}/stock — ADMIN
     * Body: { stock: 42 }
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTOs.BookResponse> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody BookDTOs.StockRequest request) {
        return ResponseEntity.ok(bookService.updateStock(id, request));
    }

    /**
     * POST /books/sync-ml — ADMIN
     * Importa produtos do Mercado Livre.
     */
    @PostMapping("/sync-ml")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookDTOs.BookResponse>> syncMercadoLivre(
            @RequestBody List<BookDTOs.MlSyncItem> items) {
        return ResponseEntity.ok(bookService.syncFromMercadoLivre(items));
    }
}
