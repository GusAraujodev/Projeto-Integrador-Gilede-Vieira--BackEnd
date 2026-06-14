package com.gilede.livraria.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.client.preference.PreferencePaymentMethodsRequest;
import com.mercadopago.client.preference.PreferencePaymentTypeRequest;
import com.mercadopago.resources.preference.Preference;
import com.gilede.livraria.model.Order;
import com.gilede.livraria.model.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Value("${mp.access-token}")
    private String accessToken;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public String createPreference(Order order) throws Exception {
        MercadoPagoConfig.setAccessToken(accessToken);

        List<PreferenceItemRequest> items = order.getItems().stream()
                .map(item -> PreferenceItemRequest.builder()
                        .title(item.getBook().getTitle())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .currencyId("BRL")
                        .build())
                .collect(Collectors.toList());

        if (order.getShippingCost() != null &&
                order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            items.add(PreferenceItemRequest.builder()
                    .title("Frete")
                    .quantity(1)
                    .unitPrice(order.getShippingCost())
                    .currencyId("BRL")
                    .build());
        }

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/pedido/" + order.getId())
                .failure(frontendUrl + "/checkout")
                .pending(frontendUrl + "/pedido/" + order.getId())
                .build();

        PreferenceRequest request = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(order.getId().toString())
                .paymentMethods(PreferencePaymentMethodsRequest.builder()
                        .excludedPaymentTypes(List.of(
                                PreferencePaymentTypeRequest.builder().id("ticket").build()
                        ))
                        .build())
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);
            return preference.getInitPoint();
        } catch (com.mercadopago.exceptions.MPApiException e) {
            String body = e.getApiResponse() != null ? e.getApiResponse().getContent() : "sem body";
            int status = e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : -1;
            throw new RuntimeException("MP API Error [" + status + "]: " + body, e);
        }
    }
}
