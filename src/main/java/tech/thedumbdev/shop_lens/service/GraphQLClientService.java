package tech.thedumbdev.shop_lens.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface GraphQLClientService {
    public <T> Mono<T> executeQuery(
            String document,
            String operationName,
            Map<String, Object> variables,
            Class<T> responseType
    );
}
