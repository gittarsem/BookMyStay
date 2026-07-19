package com.tarsem.BookMyStay.Controller;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public class WebhookController {

//    @Autowired
//    private StripeProperties stripeProperties;
//    @PostMapping("/stripe")
//    public ResponseEntity<Void> handleWebhook(
//            @RequestBody String payload,
//            @RequestHeader("Stripe-Signature")
//            String sigHeader
//    ) {
//
//        Event event;
//
//        try {
//
//            event = Webhook.constructEvent(
//                    payload,
//                    sigHeader,
//                    stripeProperties.getWebhookSecret()
//            );
//
//        } catch (Exception ex) {
//
//            return ResponseEntity.badRequest().build();
//        }
//
//        paymentService.verifyWebhook(event);
//
//        return ResponseEntity.ok().build();
//    }
}
