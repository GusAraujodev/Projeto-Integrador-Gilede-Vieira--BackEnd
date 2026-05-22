package com.gilede.livraria.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public final class MercadoLivreDTOs {

        private MercadoLivreDTOs() {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record TokenResponse(
                        @JsonProperty("access_token") String accessToken,
                        @JsonProperty("refresh_token") String refreshToken,
                        @JsonProperty("expires_in") Long expiresIn,
                        @JsonProperty("user_id") Long userId,
                        @JsonProperty("token_type") String tokenType) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ItemSearchResponse(
                        @JsonProperty("seller_id") String sellerId,
                        List<String> results,
                        Paging paging) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Paging(
                        Integer limit,
                        Integer offset,
                        Integer total) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ItemDetail(
                        String id,
                        String title,
                        @JsonProperty("category_id") String categoryId,
                        BigDecimal price,
                        @JsonProperty("available_quantity") Integer availableQuantity,
                        @JsonProperty("sold_quantity") Integer soldQuantity,
                        String status,
                        Double health,
                        List<Picture> pictures,
                        List<Attribute> attributes) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Picture(
                        String id,
                        String url,
                        @JsonProperty("secure_url") String secureUrl) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Attribute(
                        String id,
                        String name,
                        @JsonProperty("value_name") String valueName) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ItemDescription(
                        @JsonProperty("plain_text") String plainText) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record BatchItemResult(
                        Integer code,
                        ItemDetail body) {
        }
}