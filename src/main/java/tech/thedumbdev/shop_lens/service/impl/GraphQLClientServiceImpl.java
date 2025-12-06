package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tech.thedumbdev.shop_lens.service.GraphQLClientService;

import java.util.Map;

public class GraphQLClientServiceImpl implements GraphQLClientService {
    private final HttpGraphQlClient graphQlClient;

    public GraphQLClientServiceImpl(String baseUrl, String accessToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Shopify-Access-Token", accessToken)
                .build();
        this.graphQlClient = HttpGraphQlClient.create(webClient);
    }

    @Override
    public <T> Mono<T> executeQuery(String document, String operationName, Map<String, Object> variables, Class<T> responseType) {
        return graphQlClient.document(document) // The GraphQL query string
                .operationName(operationName) // Optional: name of the query/mutation
                .variables(variables) // Optional: a map of variables
                .execute()
                .map(response -> {
                    if (!response.isValid()) {
                        throw new RuntimeException("GraphQL Errors: " + response.getErrors());
                    }
                    return response.toEntity(responseType);
                });
    }
}
