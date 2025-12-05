package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tech.thedumbdev.shop_lens.service.GraphQLClientService;

import java.util.Map;

public class GraphQLClientServiceImpl implements GraphQLClientService {
    private final HttpGraphQlClient graphQlClient;

    public GraphQLClientServiceImpl(String baseUrl, String bearerToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + bearerToken)
                .build();
        this.graphQlClient = HttpGraphQlClient.create(webClient);
    }

    @Override
    public <T> Mono<T> executeQuery(String document, String operationName, Map<String, Object> variables, Class<T> responseType) {
        return graphQlClient.document(document) // The GraphQL query string
                .operationName(operationName) // Optional: name of the query/mutation
                .variables(variables) // Optional: a map of variables
                .retrieve("fieldName") // The field in the 'data' part of the response to retrieve
                .toEntity(responseType); // Decode the response to a Java object
    }
}
