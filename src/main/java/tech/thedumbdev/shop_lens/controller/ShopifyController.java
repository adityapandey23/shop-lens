package tech.thedumbdev.shop_lens.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.service.ShopifyService;

import java.sql.Timestamp;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shopify")
public class ShopifyController {

    private final ShopifyService shopifyService;

    public ShopifyController(ShopifyService shopifyService) {
        this.shopifyService = shopifyService;
    }

    @GetMapping("/install")
    public ResponseEntity<Map<String, String>> install(
            @AuthenticationPrincipal User user, // User is required to start the flow
            @RequestParam String shop
    ) {
        String url = shopifyService.getAuthorizationUrl(shop, user);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam("code") String code,
            @RequestParam("hmac") String hmac,
            @RequestParam("host") String host,
            @RequestParam("shop") String shop,
            @RequestParam("state") String state,
            @RequestParam("timestamp")String timestamp
            ) {
        shopifyService.handleOAuthCallback(
                code, hmac, host, shop, state, timestamp
        );
        return ResponseEntity.ok("App installed successfully");
    }

    // Will define webhook over here

}
