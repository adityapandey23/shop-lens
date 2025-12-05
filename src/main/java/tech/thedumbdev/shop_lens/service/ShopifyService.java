package tech.thedumbdev.shop_lens.service;

import tech.thedumbdev.shop_lens.model.User;

import java.util.Map;

public interface ShopifyService {
    public String getAuthorizationUrl(String shop, User user);
    public void handleOAuthCallback(String code, String hmac, String host, String shop, String state, String timestamp);
}
