package com.gilede.livraria.controller;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.dto.FavoriteDTOs;
import com.gilede.livraria.service.FavoritesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoritesService favoriteService;

    /** GET /favorites/user */
    @GetMapping("/user")
    public ResponseEntity<List<BookDTOs.BookResponse>> findByUser(Authentication authentication) {
        return ResponseEntity.ok(favoriteService.findByAuthenticatedUser(authentication));
    }

    /** POST /favorites */
    @PostMapping
    public ResponseEntity<BookDTOs.BookResponse> add(
            Authentication authentication,
            @Valid @RequestBody FavoriteDTOs.AddFavoriteRequest request) {
        return ResponseEntity.ok(favoriteService.add(
                authentication,
                UUID.fromString(request.bookId())));
    }

    /** DELETE /favorites/{userId}/{bookId} */
    @DeleteMapping("/{userId}/{bookId}")
    public ResponseEntity<Void> remove(
            Authentication authentication,
            @PathVariable UUID bookId) {
        favoriteService.remove(authentication, bookId);
        return ResponseEntity.noContent().build();
    }
}
