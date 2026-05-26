package com.gilede.livraria.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class FavoriteDTOs {

    public record FavoriteListResponse(List<BookDTOs.BookResponse> books) {
    }

    public record AddFavoriteRequest(
            @NotBlank String bookId) {
    }
}
