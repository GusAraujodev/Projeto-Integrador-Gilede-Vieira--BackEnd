package com.gilede.livraria.service;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.mapper.BookMapper;
import com.gilede.livraria.repository.BookRepository;
import com.gilede.livraria.model.Book;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    // ---- Área pública ----

    @Transactional(readOnly = true)
    public List<BookDTOs.BookResponse> findAllActive(String search) {
        List<Book> books;

        if (search != null && !search.isBlank()) {
            books = bookRepository.searchActiveBooks(search.trim());
        } else {
            books = bookRepository.findByActiveTrue();
        }

        return books.stream().map(bookMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookDTOs.BookResponse findById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + id));
        return bookMapper.toResponse(book);
    }

    @Transactional(readOnly = true)
    public List<BookDTOs.BookResponse> findByCategory(String category) {
        return bookRepository.findByCategoryAndActiveTrue(category)
                .stream().map(bookMapper::toResponse).toList();
    }

    // ---- Área administrativa ----

    @Transactional(readOnly = true)
    public List<BookDTOs.BookResponse> findAll() {
        return bookRepository.findAll().stream().map(bookMapper::toResponse).toList();
    }

    @Transactional
    public BookDTOs.BookResponse create(BookDTOs.BookRequest request) {
        Book book = bookMapper.toEntity(request);
        Book saved = bookRepository.save(book);
        log.info("Livro criado: {} ({})", saved.getTitle(), saved.getId());
        return bookMapper.toResponse(saved);
    }

    @Transactional
    public BookDTOs.BookResponse update(UUID id, BookDTOs.BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + id));
        bookMapper.updateEntity(book, request);
        Book saved = bookRepository.save(book);
        return bookMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Livro não encontrado: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Livro excluído: {}", id);
    }

    @Transactional
    public BookDTOs.BookResponse updateStatus(UUID id, BookDTOs.StatusRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + id));
        book.setActive(request.active());
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookDTOs.BookResponse updateStock(UUID id, BookDTOs.StockRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + id));
        book.setStock(request.stock());
        return bookMapper.toResponse(bookRepository.save(book));
    }

    /**
     * Sincronização com Mercado Livre.
     * Converte o formato ML para Book e faz upsert (cria ou atualiza pelo mlId).
     */
    @Transactional
    public List<BookDTOs.BookResponse> syncFromMercadoLivre(List<BookDTOs.MlSyncItem> items) {
        List<BookDTOs.BookResponse> result = new ArrayList<>();

        for (BookDTOs.MlSyncItem item : items) {
            Optional<Book> existingBook = bookRepository.findByMlId(item.id());

            String author = extractAttribute(item.attributes(), "author");
            String isbn = extractAttribute(item.attributes(), "isbn");
            List<String> images = item.pictures() == null
                    ? List.of()
                    : item.pictures().stream().map(BookDTOs.MlPicture::url).toList();

            Book book;
            if (existingBook.isPresent()) {
                book = existingBook.get();
            book.setTitle(item.title());
                book.setDescription(item.description());
                book.setCategory(item.category());
            book.setPrice(item.price());
            book.setStock(item.available_quantity());
            book.setImages(images);
            book.setMlSynced(true);
                book.setSalesCount(item.soldQuantity() != null ? item.soldQuantity() : book.getSalesCount());
                book.setRating(item.health() != null ? item.health().doubleValue() : book.getRating());
            } else {
                book = Book.builder()
                        .title(item.title())
                        .author(author != null ? author : "Desconhecido")
                        .description(item.description())
                        .category(item.category())
                        .price(item.price())
                        .stock(item.available_quantity())
                        .salesCount(item.soldQuantity() != null ? item.soldQuantity() : 0)
                        .images(images)
                        .active(true)
                        .mlId(item.id())
                        .mlSynced(true)
                        .isbn(isbn)
                        .rating(item.health() != null ? item.health().doubleValue() : null)
                        .build();
            }

            result.add(bookMapper.toResponse(bookRepository.save(book)));
        }

        log.info("Sincronização ML concluída: {} itens processados", result.size());
        return result;
    }

    private String extractAttribute(List<BookDTOs.MlAttribute> attributes, String name) {
        if (attributes == null)
            return null;
        return attributes.stream()
                .filter(a -> name.equalsIgnoreCase(a.name()))
                .map(BookDTOs.MlAttribute::value)
                .findFirst()
                .orElse(null);
    }

    // ---- Utilitário interno (usado pelo OrderService) ----

    @Transactional
    public void decrementStock(UUID bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + bookId));

        if (book.getStock() < quantity) {
            throw new IllegalStateException(
                    "Estoque insuficiente para o livro: " + book.getTitle() +
                            " (disponível: " + book.getStock() + ", solicitado: " + quantity + ")");
        }

        book.setStock(book.getStock() - quantity);
        bookRepository.save(book);
    }
}
