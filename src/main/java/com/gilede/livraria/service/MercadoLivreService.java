package com.gilede.livraria.service;

import com.gilede.livraria.dto.MercadoLivreDTOs.Attribute;
import com.gilede.livraria.dto.MercadoLivreDTOs.BatchItemResult;
import com.gilede.livraria.dto.MercadoLivreDTOs.ItemDescription;
import com.gilede.livraria.dto.MercadoLivreDTOs.ItemDetail;
import com.gilede.livraria.dto.MercadoLivreDTOs.ItemSearchResponse;
import com.gilede.livraria.dto.MercadoLivreDTOs.Paging;
import com.gilede.livraria.dto.MercadoLivreDTOs.Picture;
import com.gilede.livraria.dto.MercadoLivreDTOs.TokenResponse;
import com.gilede.livraria.model.Book;
import com.gilede.livraria.model.MercadoLivreConfig;
import com.gilede.livraria.repository.BookRepository;
import com.gilede.livraria.repository.MercadoLivreConfigRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoLivreService {

    private static final int SEARCH_PAGE_SIZE = 50;
    private static final int BATCH_SIZE = 20;
    private static final long RATE_LIMIT_DELAY_MS = 50L;
    private static final String TOKEN_URL = "https://api.mercadolibre.com/oauth/token";
    private static final String ITEMS_SEARCH_URL = "https://api.mercadolibre.com/users/%s/items/search";
    private static final String ITEM_BATCH_URL = "https://api.mercadolibre.com/items";
    private static final String ITEM_DESCRIPTION_URL = "https://api.mercadolibre.com/items/%s/description";

    private final MercadoLivreConfigRepository configRepository;
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.client-id:}")
    private String clientId;

    @Value("${ml.client-secret:}")
    private String clientSecret;

    @Value("${ml.seller-id}")
    private String sellerId;

    @Value("${ml.redirect-uri:}")
    private String redirectUri;

    @Transactional
    public MercadoLivreConfig exchangeCodeForTokens(@NonNull String code) {
        validateMlCredentials();
        log.info("Exchanging Mercado Livre authorization code for tokens");

        LinkedMultiValueMap<String, String> form = formData("authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        TokenResponse tokenResponse = requestToken(form);
        return saveTokens(tokenResponse);
    }

    public MercadoLivreConfig exchangeAuthorizationCode(String code) {
        return exchangeCodeForTokens(code);
    }

    @Transactional
    public MercadoLivreConfig refreshAccessToken() {
        validateMlCredentials();
        MercadoLivreConfig config = getConfigOrThrow();

        if (!StringUtils.hasText(config.getRefreshToken())) {
            throw new IllegalStateException("Refresh token ausente. Refaça a autenticação do Mercado Livre.");
        }

        log.info("Refreshing Mercado Livre access token for seller {}", config.getSellerId());
        LinkedMultiValueMap<String, String> form = formData("refresh_token");
        form.add("refresh_token", config.getRefreshToken());

        TokenResponse tokenResponse = requestToken(form);
        return saveTokens(tokenResponse);
    }

    public String getValidAccessToken() {
        MercadoLivreConfig config = getConfigOrThrow();
        if (config.isExpired()) {
            log.info("Mercado Livre token expired or near expiration, refreshing automatically");
            config = refreshAccessToken();
        }

        if (!StringUtils.hasText(config.getAccessToken())) {
            throw new IllegalStateException("Token de acesso do Mercado Livre indisponível.");
        }

        return config.getAccessToken();
    }

    public int syncCatalog() {
        String accessToken = getValidAccessToken();
        List<String> itemIds = fetchActiveItemIds(accessToken);
        int processedBooks = 0;

        for (List<String> batch : partition(itemIds, BATCH_SIZE)) {
            List<BatchItemResult> batchResults = fetchBatchItems(batch, accessToken);

            for (BatchItemResult result : batchResults) {
                if (result == null || result.code() == null || result.code() < 200 || result.code() >= 300
                        || result.body() == null) {
                    continue;
                }

                String itemId = result.body().id();
                if (!StringUtils.hasText(itemId)) {
                    continue;
                }

                String description = fetchDescription(itemId, accessToken);
                upsertBook(itemId, result.body(), description);
                processedBooks++;

                try {
                    Thread.sleep(RATE_LIMIT_DELAY_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Sincronização do Mercado Livre interrompida", ex);
                }
            }
        }

        log.info("Mercado Livre sync completed with {} books processed", processedBooks);
        return processedBooks;
    }

    public ItemDetail fetchItemDetail(String itemId) {
        if (!StringUtils.hasText(itemId)) {
            throw new IllegalStateException("Item ID do Mercado Livre inválido.");
        }

        String accessToken = getValidAccessToken();
        ResponseEntity<ItemDetail> response = restTemplate.exchange(
                ITEM_BATCH_URL + "/" + itemId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(accessToken)),
                ItemDetail.class);

        ItemDetail item = response.getBody();
        if (item == null || !StringUtils.hasText(item.id())) {
            throw new IllegalStateException("Item do Mercado Livre não encontrado para o id " + itemId);
        }

        return item;
    }

    private MercadoLivreConfig saveTokens(@NonNull TokenResponse tokenResponse) {
        MercadoLivreConfig config = configRepository.findTopByOrderByIdDesc().orElseGet(MercadoLivreConfig::new);

        // Usar o userId real retornado pelo ML; só cai no SELLER_ID hardcoded como
        // fallback
        if (tokenResponse.userId() != null && tokenResponse.userId() > 0) {
            config.setSellerId(tokenResponse.userId().toString());
        } else if (!StringUtils.hasText(config.getSellerId())) {
            config.setSellerId(sellerId);
        }

        config.setAccessToken(tokenResponse.accessToken());

        // No fluxo de refresh o ML NÃO retorna novo refresh_token — isso é normal.
        // Só sobrescreve se vier um refresh_token novo na resposta.
        if (StringUtils.hasText(tokenResponse.refreshToken())) {
            config.setRefreshToken(tokenResponse.refreshToken());
        }

        if (!StringUtils.hasText(config.getRefreshToken())) {
            log.warn("Refresh token não recebido do Mercado Livre. O token será renovado manualmente se necessário.");
        }

        long expiresIn = tokenResponse.expiresIn() != null ? tokenResponse.expiresIn() : 3600L;
        config.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));

        MercadoLivreConfig saved = configRepository.save(config);
        log.info("Mercado Livre tokens saved or updated for seller {}", saved.getSellerId());
        return saved;
    }

    private MercadoLivreConfig getConfigOrThrow() {
        return configRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("Mercado Livre não conectado para o seller " + sellerId));
    }

    private TokenResponse requestToken(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                TOKEN_URL,
                new HttpEntity<>(form, headers),
                TokenResponse.class);

        TokenResponse body = response.getBody();
        if (body == null || !StringUtils.hasText(body.accessToken())) {
            throw new IllegalStateException("Falha ao obter tokens do Mercado Livre");
        }
        return body;
    }

    private void validateMlCredentials() {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret) || !StringUtils.hasText(redirectUri)) {
            throw new IllegalStateException("Credenciais do Mercado Livre não configuradas no ambiente");
        }
    }

    private LinkedMultiValueMap<String, String> formData(String grantType) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", grantType);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return form;
    }

    private List<String> fetchActiveItemIds(String accessToken) {
        log.info("Fetching active Mercado Livre item ids for seller {}", sellerId);
        Set<String> itemIds = new LinkedHashSet<>();
        int offset = 0;

        while (true) {
            ItemSearchResponse response = fetchItemSearchPage(accessToken, SEARCH_PAGE_SIZE, offset);
            if (response == null || response.results() == null || response.results().isEmpty()) {
                break;
            }

            itemIds.addAll(response.results());

            Paging paging = response.paging();
            Integer limit = paging != null && paging.limit() != null ? paging.limit() : SEARCH_PAGE_SIZE;
            Integer total = paging != null ? paging.total() : null;
            offset += limit;

            if (total != null && offset >= total) {
                break;
            }

            if (response.results().size() < limit) {
                break;
            }
        }

        return new ArrayList<>(itemIds);
    }

    private ItemSearchResponse fetchItemSearchPage(String accessToken, int limit, int offset) {
        String url = UriComponentsBuilder
                .fromHttpUrl(ITEMS_SEARCH_URL.formatted(getConfigOrThrow().getSellerId()))
                .queryParam("status", "active")
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .toUriString();

        ResponseEntity<ItemSearchResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(accessToken)),
                ItemSearchResponse.class);

        return response.getBody();
    }

    private List<BatchItemResult> fetchBatchItems(List<String> batchIds, String accessToken) {
        String url = UriComponentsBuilder
                .fromHttpUrl(ITEM_BATCH_URL)
                .queryParam("ids", String.join(",", batchIds))
                .toUriString();

        ResponseEntity<BatchItemResult[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(accessToken)),
                BatchItemResult[].class);

        return Optional.ofNullable(response.getBody())
                .map(Arrays::asList)
                .orElse(List.of());
    }

    private String fetchDescription(String itemId, String accessToken) {
        ResponseEntity<ItemDescription> response = restTemplate.exchange(
                ITEM_DESCRIPTION_URL.formatted(itemId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(accessToken)),
                ItemDescription.class);

        return Optional.ofNullable(response.getBody())
                .map(ItemDescription::plainText)
                .orElse("");
    }

    private HttpHeaders authHeaders(@NonNull String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private Book upsertBook(@NonNull String itemId, @NonNull ItemDetail item, @NonNull String description) {
        Book book = bookRepository.findByMlId(itemId).orElseGet(Book::new);

        String author = firstText(
                extractAttribute(item.attributes(), "AUTHOR"),
                extractAttribute(item.attributes(), "AUTHOR_NAME"),
                extractAttribute(item.attributes(), "BRAND"),
                book.getAuthor(),
                "Mercado Livre");
        String isbn = firstText(extractAttribute(item.attributes(), "ISBN"), book.getIsbn());
        String publisher = firstText(
                extractAttribute(item.attributes(), "PUBLISHER", "BOOK_PUBLISHER", "PUBLICATION_NAME"),
                book.getPublisher());
        Integer pages = firstInteger(
                extractIntegerAttribute(item.attributes(), "PAGES", "BOOK_PAGES", "PAGE_COUNT"),
                book.getPages());
        String category = firstText(book.getCategory(), item.categoryId(), "Mercado Livre");
        BigDecimal price = item.price() != null ? item.price() : book.getPrice();
        Double rating = item.health() != null ? Math.max(0.0, Math.min(5.0, item.health() * 5.0)) : book.getRating();

        book.setTitle(firstText(item.title(), book.getTitle(), "Mercado Livre"));
        book.setAuthor(author);
        book.setDescription(firstText(description, book.getDescription()));
        book.setCategory(category);
        book.setPrice(price != null ? price : BigDecimal.ZERO);
        book.setStock(item.availableQuantity() != null ? item.availableQuantity() : 0);
        book.setSalesCount(item.soldQuantity() != null ? item.soldQuantity() : 0);
        book.setImages(extractImages(item));
        book.setActive(true);
        book.setMlId(itemId);
        book.setMlSynced(true);
        book.setRating(rating);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setPages(pages);

        return bookRepository.save(book);
    }

    private List<String> extractImages(ItemDetail item) {
        return Optional.ofNullable(item.pictures())
                .orElse(List.of())
                .stream()
                .map(this::bestPictureUrl)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String bestPictureUrl(Picture picture) {
        return firstText(picture.secureUrl(), picture.url());
    }

    private String extractAttribute(List<Attribute> attributes, String... names) {
        return Optional.ofNullable(attributes)
                .orElse(List.of())
                .stream()
                .filter(attribute -> matchesAny(attribute, names))
                .map(Attribute::valueName)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private Integer extractIntegerAttribute(List<Attribute> attributes, String... names) {
        String value = extractAttribute(attributes, names);
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String digitsOnly = value.replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digitsOnly)) {
            return null;
        }

        try {
            return Integer.valueOf(digitsOnly);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer firstInteger(Integer... values) {
        for (Integer value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private boolean matchesAny(Attribute attribute, String... names) {
        if (attribute == null || names == null) {
            return false;
        }

        for (String name : names) {
            if (name != null && (name.equalsIgnoreCase(attribute.name()) || name.equalsIgnoreCase(attribute.id()))) {
                return true;
            }
        }

        return false;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private List<List<String>> partition(List<String> values, int size) {
        if (values.isEmpty()) {
            return List.of();
        }

        List<List<String>> partitions = new ArrayList<>();
        for (int index = 0; index < values.size(); index += size) {
            partitions.add(values.subList(index, Math.min(index + size, values.size())));
        }
        return partitions;
    }
}