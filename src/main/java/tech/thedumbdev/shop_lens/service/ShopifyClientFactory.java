package tech.thedumbdev.shop_lens.service;

public interface ShopifyClientFactory {
    public GraphQLClientService createClient(String shopDomain, String accessToken);
}
