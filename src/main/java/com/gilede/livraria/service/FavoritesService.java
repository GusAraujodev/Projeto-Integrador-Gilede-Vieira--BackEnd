package com.gilede.livraria.service;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.mapper.BookMapper;
import com.gilede.livraria.repository.BookRepository;
import com.gilede.livraria.repository.FavoriteRepository;
import com.gilede.livraria.repository.UserRepository;
import com.gilede.livraria.model.Book;
import com.gilede.livraria.model.Favorite;
import com.gilede.livraria.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoritesService {

    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public List<BookDTOs.BookResponse> findByUserId(UUID userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(fav -> bookMapper.toResponse(fav.getBook()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookDTOs.BookResponse> findByAuthenticatedUser(Authentication authentication) {
        UUID userId = resolveAuthenticatedUserId(authentication);
        return findByUserId(userId);
    }

    @Transactional
    public BookDTOs.BookResponse add(Authentication authentication, UUID bookId) {
        UUID userId = resolveAuthenticatedUserId(authentication);

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
    public void remove(Authentication authentication, UUID bookId) {
        UUID userId = resolveAuthenticatedUserId(authentication);
        favoriteRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    private UUID resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório para favoritar livros");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório para favoritar livros");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não encontrado"));
        return user.getId();
    }
}
