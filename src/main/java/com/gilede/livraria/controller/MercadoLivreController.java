package com.gilede.livraria.controller;

import com.gilede.livraria.model.MercadoLivreConfig;
import com.gilede.livraria.service.MercadoLivreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/ml")
@Slf4j
public class MercadoLivreController {

    private static final String AUTHORIZATION_BASE_URL = "https://auth.mercadolivre.com.br/authorization";

    private final MercadoLivreService mercadoLivreService;
    private final String frontendUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public MercadoLivreController(MercadoLivreService mercadoLivreService,
                                  @Value("${app.frontend-url}") String frontendUrl,
                                  @Value("${ml.client-id}") String clientId,
                                  @Value("${ml.client-secret}") String clientSecret,
                                  @Value("${ml.redirect-uri}") String redirectUri) {
        this.mercadoLivreService = mercadoLivreService;
        this.frontendUrl = frontendUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> authUrl() {
        String authorizationUrl = UriComponentsBuilder.fromHttpUrl(AUTHORIZATION_BASE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build(true)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "authorizationUrl", authorizationUrl,
                "instruction", "Abra a URL, autorize o aplicativo e envie o code para /api/ml/callback",
                "redirectUri", redirectUri,
                "clientSecretConfigured", String.valueOf(clientSecret != null && !clientSecret.isBlank())
        ));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code) {
        try {
            mercadoLivreService.exchangeCodeForTokens(code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/admin/ml-sync?ml=connected"))
                    .build();
        } catch (Exception ex) {
            log.error("Erro no callback OAuth do Mercado Livre: {}", ex.getMessage(), ex);
            String errorMsg = java.net.URLEncoder.encode(
                ex.getMessage() != null ? ex.getMessage() : "erro_desconhecido",
                java.nio.charset.StandardCharsets.UTF_8
            );
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendUrl + "/admin/ml-sync?ml=error&reason=" + errorMsg))
                    .build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        long startedAt = System.currentTimeMillis();
        int synced = mercadoLivreService.syncCatalog();
        long durationMs = System.currentTimeMillis() - startedAt;
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "books", synced,
                "durationMs", durationMs
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        try {
            String accessToken = mercadoLivreService.getValidAccessToken();
            return ResponseEntity.ok(Map.of(
                    "authorized", true,
                    "accessTokenActive", true,
                    "tokenSuffix", accessToken.length() > 6 ? accessToken.substring(accessToken.length() - 6) : accessToken
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "authorized", false,
                    "message", "Conta do Mercado Livre ainda não autorizada ou token expirado: " + ex.getMessage()
            ));
        }
    }
}