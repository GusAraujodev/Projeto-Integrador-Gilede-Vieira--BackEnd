package com.gilede.livraria.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
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

        PreferenceRequest request = PreferenceRequest.builder()
            .items(items)
            .externalReference(order.getId().toString())
            .autoReturn("approved")
            .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(request);
        return preference.getInitPoint();
    }
}
