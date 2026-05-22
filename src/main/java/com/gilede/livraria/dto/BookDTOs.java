package com.gilede.livraria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class BookDTOs {

    /** Response — espelhado exatamente ao contrato do frontend */
    public record BookResponse(
            String id,
            String title,
            String author,
            String description,
            String category,
            BigDecimal price,
            Integer stock,
            List<String> images,
            Boolean active,
            Integer year,
            String mlId,
            Boolean mlSynced,
            Integer salesCount,
            Double rating,
            String isbn,
            String publisher,
            Integer pages,
            List<ReviewResponse> reviews) {
    }

    public record ReviewResponse(
            String id,
            String userId,
            String userName,
            Integer rating,
            String comment,
            String date // ISO-8601 string para o frontend
    ) {
    }

    /** Request para criar ou atualizar livro */
    public record BookRequest(
            @NotBlank String title,
            @NotBlank String author,
            String description,
            @NotBlank String category,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            List<String> images,
            Boolean active,
            Integer year,
            String mlId,
            Boolean mlSynced,
            Double rating,
            String isbn) {
    }

    /** Payload para PATCH /books/:id/status */
    public record StatusRequest(
            @NotNull Boolean active) {
    }

    /** Payload para PATCH /books/:id/stock */
    public record StockRequest(
            @NotNull @Min(0) Integer stock) {
    }

    /** Payload de sincronização com Mercado Livre */
    public record MlSyncItem(
            String id,
            String title,
            String category,
            BigDecimal price,
            Integer available_quantity,
            String description,
            @JsonProperty("sold_quantity") Integer soldQuantity,
            Integer health,
            List<MlPicture> pictures,
            List<MlAttribute> attributes) {
    }

    public record MlPicture(String url) {
    }

    public record MlAttribute(String name, String value) {
    }
}
