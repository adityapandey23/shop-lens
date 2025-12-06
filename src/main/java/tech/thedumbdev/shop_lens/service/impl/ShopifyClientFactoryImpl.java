package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.stereotype.Service;
import tech.thedumbdev.shop_lens.service.GraphQLClientService;
import tech.thedumbdev.shop_lens.service.ShopifyClientFactory;

@Service
public class ShopifyClientFactoryImpl implements ShopifyClientFactory {
    @Override
    public GraphQLClientService createClient(String shopDomain, String accessToken) {
        String baseUrl = String.format("https://%s/admin/api/2025-10/graphql.json", shopDomain);
        return new GraphQLClientServiceImpl(baseUrl, accessToken);
    }
}
