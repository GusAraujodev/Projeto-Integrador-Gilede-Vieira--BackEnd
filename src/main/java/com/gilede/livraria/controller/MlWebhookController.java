package com.gilede.livraria.controller;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.dto.MercadoLivreDTOs.ItemDetail;
import com.gilede.livraria.service.MercadoLivreService;
import com.gilede.livraria.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/ml")
@RequiredArgsConstructor
public class MlWebhookController {

    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("/items/([^/?]+)");

    private final BookService bookService;
    private final MercadoLivreService mercadoLivreService;

    public record MlWebhookPayload(String resource, String topic) {
    }

    @PostMapping({"/test-mock-payload", "/webhook"})
    public ResponseEntity<?> testMockPayload(@RequestBody MlWebhookPayload payload) {
        try {
            String itemId = extractItemId(payload.resource());
            ItemDetail item = mercadoLivreService.fetchItemDetail(itemId);

            BookDTOs.BookRequest request = new BookDTOs.BookRequest(
                    item.title(),
                    "Mercado Livre",
                    null,
                    item.categoryId() != null ? item.categoryId() : "Mercado Livre",
                    item.price() != null ? item.price() : BigDecimal.ZERO,
                    item.availableQuantity() != null ? item.availableQuantity() : 0,
                    List.of(),
                    true,
                    null,
                    item.id(),
                    true,
                    item.health() != null ? Math.max(0.0, Math.min(5.0, item.health() * 5.0)) : null,
                    null
            );

            BookDTOs.BookResponse savedBook = bookService.create(request);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("causa_real", e.getMessage()));
        }
    }

    private String extractItemId(String resource) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalStateException("Webhook do Mercado Livre sem campo resource.");
        }

        Matcher matcher = ITEM_ID_PATTERN.matcher(resource);
        if (matcher.find() && matcher.group(1) != null && !matcher.group(1).isBlank()) {
            return matcher.group(1);
        }

        if (resource.startsWith("ML")) {
            return resource;
        }

        throw new IllegalStateException("Não foi possível extrair itemId do resource: " + resource);
    }
}