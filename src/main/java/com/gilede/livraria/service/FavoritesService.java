package com.gilede.livraria.service;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.mapper.BookMapper;
import com.gilede.livraria.repository.BookRepository;
import com.gilede.livraria.repository.FavoriteRepository;
import com.gilede.livraria.model.Book;
import com.gilede.livraria.model.Favorite;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoritesService {

    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public List<BookDTOs.BookResponse> findByUserId(UUID userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(fav -> bookMapper.toResponse(fav.getBook()))
                .toList();
    }

    @Transactional
    public BookDTOs.BookResponse add(UUID userId, UUID bookId) {
        if (favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            // Idempotente — retorna o livro sem erro
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + bookId));
            return bookMapper.toResponse(book);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado: " + bookId));

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .book(book)
                .build();

        favoriteRepository.save(favorite);
        return bookMapper.toResponse(book);
    }

    @Transactional
    public void remove(UUID userId, UUID bookId) {
        favoriteRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}
