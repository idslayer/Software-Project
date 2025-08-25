//package com.bookingservice.util;
//
//import com.stripe.model.Event;
//import com.stripe.net.Webhook;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StripeWebhookUtil {
//    @Value("${stripe.webhook-secret}")
//    private String webhookSecret;
//
//    public Event constructEvent(String payload, String sigHeader) {
//        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
//    }
//}
