package tech.thedumbdev.shop_lens.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for verifying Shopify webhook HMAC signatures.
 * Shopify webhooks use Base64-encoded HMAC-SHA256 of the raw request body.
 */
public class WebhookHmacUtil {

    private WebhookHmacUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Verifies the HMAC signature of a Shopify webhook request.
     *
     * @param requestBody The raw request body as a string
     * @param hmacHeader  The value of X-Shopify-Hmac-Sha256 header
     * @param secret      The Shopify API secret
     * @return true if the HMAC is valid, false otherwise
     */
    public static boolean isValidHmac(String requestBody, String hmacHeader, String secret) {
        if (requestBody == null || hmacHeader == null || secret == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(requestBody.getBytes(StandardCharsets.UTF_8));
            String computedHmac = Base64.getEncoder().encodeToString(hash);

            return computedHmac.equals(hmacHeader);
        } catch (Exception e) {
            return false;
        }
    }
}
