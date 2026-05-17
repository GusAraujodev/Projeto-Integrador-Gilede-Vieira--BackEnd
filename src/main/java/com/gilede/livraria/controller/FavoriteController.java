package com.gilede.livraria.controller;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.dto.FavoriteDTOs;
import com.gilede.livraria.service.FavoritesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoritesService favoriteService;

    /** GET /favorites/user/{userId} */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookDTOs.BookResponse>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(favoriteService.findByUserId(userId));
    }

    /** POST /favorites */
    @PostMapping
    public ResponseEntity<BookDTOs.BookResponse> add(
            @Valid @RequestBody FavoriteDTOs.AddFavoriteRequest request) {
        return ResponseEntity.ok(favoriteService.add(
                UUID.fromString(request.userId()),
                UUID.fromString(request.bookId())));
    }

    /** DELETE /favorites/{userId}/{bookId} */
    @DeleteMapping("/{userId}/{bookId}")
    public ResponseEntity<Void> remove(
            @PathVariable UUID userId,
            @PathVariable UUID bookId) {
        favoriteService.remove(userId, bookId);
        return ResponseEntity.noContent().build();
    }
}
