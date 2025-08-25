package com.bookingservice.service;


import com.bookingservice.dto.ProductRequest;
import com.bookingservice.dto.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Slf4j
@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;
    @Value("${fe-server.url}")
    private String serverFe;
    //stripe -API
    //-> productName , amount , quantity , currency
    //-> return sessionId and url



        public StripeResponse checkoutProducts(ProductRequest productRequest) {
            // Set your secret key. Remember to switch to your live secret key in production!
            Stripe.apiKey = secretKey;

            // Create a PaymentIntent with the order amount and currency
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(productRequest.getName())
                            .build();

            // Create new line item with the above product data and associated price
            BigDecimal priceBd = BigDecimal.valueOf(productRequest.getAmount());
            long priceInPence = priceBd
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("GBP")
                            .setUnitAmount(priceInPence)
                            .setProductData(productData)
                            .build();

            // Create new line item with the above price data
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams
                            .LineItem.builder()
                            .setQuantity(productRequest.getQuantity())
                            .setPriceData(priceData)
                            .build();

            // Create new session with the line items
            log.error("ssadasda:" + productRequest.getPaymentId());
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(serverFe+ "payment/success/" + productRequest.getPaymentId())
                            .setCancelUrl(serverFe+ "payment/fail/" + productRequest.getPaymentId())
                            .addLineItem(lineItem)
                            .build();

            // Create new session
            Session session = null;
            try {
                session = Session.create(params);

            } catch (StripeException e) {
                //log the error
                log.error("Stripe error while creating session: {}", e.getMessage());
            }

            return StripeResponse
                    .builder()
                    .status("SUCCESS")
                    .message("Payment session created ")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        }

}
