package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.thedumbdev.shop_lens.model.Tenant;
import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.repository.TenantRepo;
import tech.thedumbdev.shop_lens.repository.UserRepo;
import tech.thedumbdev.shop_lens.service.ShopifyOauthService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopifyOauthServiceImpl implements ShopifyOauthService {

    private String apiKey;
    private String apiSecret;
    private String appHost;
    private String scopes;

    private final TenantRepo tenantRepo;
    private final UserRepo userRepo;
    private final RestTemplate restTemplate;

    public ShopifyOauthServiceImpl(
            TenantRepo tenantRepo,
            UserRepo userRepo,
            @Value("${shopify.api.key}") String apiKey,
            @Value("${shopify.api.secret}") String apiSecret,
            @Value("${shopify.app.host}") String appHost,
            @Value("${shopify.api.scopes}") String scopes
    ) {
        this.tenantRepo = tenantRepo;
        this.userRepo = userRepo;
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.appHost = appHost;
        this.scopes = scopes;
    }

    @Override
    public String getAuthorizationUrl(String shop, User user) {
        String redirectUri = appHost + "/api/v1/shopify/callback"; // Fixed path to include /v1

        // Generate a state param containing the User ID (Base64 encoded for simplicity)
        // In production, this should be a signed token or a random nonce stored in Redis
        String state = Base64.getEncoder().encodeToString(user.getId().toString().getBytes(StandardCharsets.UTF_8));

        return String.format("https://%s/admin/oauth/authorize?client_id=%s&scope=%s&redirect_uri=%s&state=%s",
                shop, apiKey, scopes, redirectUri, state);
    }

    @Override
    public void handleOAuthCallback(
            String code,
            String hmac,
            String host,
            String shop,
            String state,
            String timestamp
    ) {

        if (shop == null || code == null || hmac == null) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        // --- STEP 1: Reconstruct the Map for Validation ---
        // We must put all the params (except hmac) into a map to sort and hash them.
        Map<String, String> paramsToValidate = new HashMap<>();
        paramsToValidate.put("code", code);
        paramsToValidate.put("host", host);
        paramsToValidate.put("shop", shop);
        paramsToValidate.put("state", state);
        paramsToValidate.put("timestamp", timestamp); // Essential for security

        // Pass the map and the received HMAC to the validator
        if (!isHmacValid(paramsToValidate, hmac, apiSecret)) {
            throw new RuntimeException("Invalid HMAC signature: Request could be tampered");
        }

        User user = recoverUserFromState(state);

        String accessTokenUrl = String.format("https://%s/admin/oauth/access_token", shop);
        Map<String, String> payload = new HashMap<>();
        payload.put("client_id", apiKey);
        payload.put("client_secret", apiSecret);
        payload.put("code", code);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(accessTokenUrl, payload, Map.class);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                saveTenantToken(user, shop, accessToken);
            }
            else {
                throw new RuntimeException("Failed to retrieve access token from Shopify");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error exchanging token with Shopify", e);
        }
    }

    // Helper Function
    private void saveTenantToken(User user, String shop, String accessToken) {
        Optional<Tenant> existingTenant = tenantRepo.findByShopDomain(shop);

        Tenant tenant;
        if(existingTenant.isPresent()) {
            tenant = existingTenant.get();
            tenant.setAccessToken(accessToken);
        }
        else {
            tenant = new Tenant();
            tenant.setShopDomain(shop);
            tenant.setAccessToken(accessToken);
            user.setTenant(tenant);
            userRepo.save(user);
        }

        tenantRepo.save(tenant);

        System.out.println("Successfully installed the app and saved token for " + shop);
    }

    private User recoverUserFromState(String state) {
        try {
            String userIdStr = new String(Base64.getDecoder().decode(state), StandardCharsets.UTF_8);
            UUID userId = UUID.fromString(userIdStr);
            return userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found from state"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid state parameter", e);
        }
    }

    private boolean isHmacValid(Map<String, String> params, String receivedHmac, String secret) {
        try {
            // 1. Construct the message: "code=...&host=...&shop=...&state=...&timestamp=..."
            // It MUST be sorted alphabetically by key.
            String message = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            // 2. Hash it
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            // 3. Convert to Hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // 4. Compare
            return hexString.toString().equals(receivedHmac);
        } catch (Exception e) {
            return false;
        }
    }

}
