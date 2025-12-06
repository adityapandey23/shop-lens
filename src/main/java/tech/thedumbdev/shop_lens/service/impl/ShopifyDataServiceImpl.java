package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.thedumbdev.shop_lens.dto.CustomerGraphqlResponse;
import tech.thedumbdev.shop_lens.dto.OrderGraphqlResponse;
import tech.thedumbdev.shop_lens.dto.ProductGraphqlResponse;
import tech.thedumbdev.shop_lens.dto.ShopifyPageResult;
import tech.thedumbdev.shop_lens.graphql_objects.orders.OrderEdgeNode;
import tech.thedumbdev.shop_lens.graphql_objects.products.ProductEdgeNode;
import tech.thedumbdev.shop_lens.graphql_objects.customers.CustomerEdgeNode;
import tech.thedumbdev.shop_lens.model.Customer;
import tech.thedumbdev.shop_lens.model.Order;
import tech.thedumbdev.shop_lens.model.Product;
import tech.thedumbdev.shop_lens.model.Tenant;
import tech.thedumbdev.shop_lens.model.enums.EntityType;
import tech.thedumbdev.shop_lens.model.enums.FinancialStatus;
import tech.thedumbdev.shop_lens.model.enums.FulfillmentStatus;
import tech.thedumbdev.shop_lens.repository.CustomerRepo;
import tech.thedumbdev.shop_lens.repository.OrderRepo;
import tech.thedumbdev.shop_lens.repository.ProductRepo;
import tech.thedumbdev.shop_lens.repository.TenantRepo;
import tech.thedumbdev.shop_lens.service.GraphQLClientService;
import tech.thedumbdev.shop_lens.service.ShopifyClientFactory;
import tech.thedumbdev.shop_lens.service.ShopifyDataService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShopifyDataServiceImpl implements ShopifyDataService {

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final CustomerRepo customerRepo;
    private final TenantRepo tenantRepo;
    private final ShopifyClientFactory shopifyClientFactory;

    public ShopifyDataServiceImpl(
            OrderRepo orderRepo,
            ProductRepo productRepo,
            CustomerRepo customerRepo,
            TenantRepo tenantRepo,
            ShopifyClientFactory clientFactory
    ) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.tenantRepo = tenantRepo;
        this.shopifyClientFactory = clientFactory;
    }

    @Override
    public ShopifyPageResult<?> fetchPage(
            String shopDomain,
            String accessToken,
            String cursor,
            EntityType entityType
    ) {
        GraphQLClientService clientService = shopifyClientFactory.createClient(shopDomain, accessToken);

        String query = buildQueryForEntityType(entityType);
        
        // FIX: Use null for first page, not empty string
        java.util.HashMap<String, Object> variables = new java.util.HashMap<>();
        variables.put("cursor", (cursor != null && !cursor.isEmpty()) ? cursor : null);

        try {
            if(entityType.equals(EntityType.ORDERS)) {
                OrderGraphqlResponse response = clientService.executeQuery(
                        query,
                        null,
                        variables,
                        OrderGraphqlResponse.class
                ).block();
                return mapOrderResponse(response);
            }
            else if(entityType.equals(EntityType.PRODUCTS)) {
                ProductGraphqlResponse response = clientService.executeQuery(
                        query,
                        null,
                        variables,
                        ProductGraphqlResponse.class
                ).block();
                return mapProductResponse(response);
            }
            else {
                CustomerGraphqlResponse response = clientService.executeQuery(
                        query,
                        null,
                        variables,
                        CustomerGraphqlResponse.class
                ).block();
                return mapCustomerResponse(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Shopify Fetch Error", e);
        }
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void savePageData(List<?> data, EntityType entityType, UUID tenantId) {
        if (data == null || data.isEmpty()) return;

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));

        if (entityType.equals(EntityType.ORDERS)) {
            List<Order> orders = (List<Order>) data;
            orders.forEach(order -> order.setTenant(tenant));
            orderRepo.saveAll(orders);
        } else if (entityType.equals(EntityType.PRODUCTS)) {
            List<Product> products = (List<Product>) data;
            products.forEach(product -> product.setTenant(tenant));
            productRepo.saveAll(products);
        } else {
            List<Customer> customers = (List<Customer>) data;
            customers.forEach(customer -> customer.setTenant(tenant));
            customerRepo.saveAll(customers);
        }
    }

    private String buildQueryForEntityType(EntityType entityType) {
        return switch (entityType) {
            case EntityType.ORDERS -> """
                    query($cursor: String) {
                      orders(first: 50, after: $cursor, sortKey: CREATED_AT) {
                        edges {
                          cursor
                          node {
                            id
                            name
                            createdAt
                            totalPriceSet {
                              shopMoney {
                                amount
                                currencyCode
                              }
                            }
                            displayFinancialStatus
                            displayFulfillmentStatus
                            customer {
                              id
                              email
                            }
                          }
                        }
                        pageInfo {
                          hasNextPage
                          endCursor
                        }
                      }
                    }
                """;

            case EntityType.PRODUCTS -> """
                    query($cursor: String) {
                      products(first: 50, after: $cursor, sortKey: CREATED_AT) {
                        edges {
                          cursor
                          node {
                            id
                            title
                            totalInventory
                            publishedAt
                            createdAt
                            variants(first: 1) {
                              edges {
                                node {
                                  price
                                }
                              }
                            }
                          }
                        }
                        pageInfo {
                          hasNextPage
                          endCursor
                        }
                      }
                    }
                """;

            case EntityType.CUSTOMERS -> """
                    query($cursor: String) {
                      customers(first: 50, after: $cursor, sortKey: CREATED_AT) {
                        edges {
                          cursor
                          node {
                            id
                            firstName
                            lastName
                            email
                            createdAt
                            numberOfOrders
                            amountSpent {
                              amount
                              currencyCode
                            }
                            lastOrder {
                              createdAt
                            }
                          }
                        }
                        pageInfo {
                          hasNextPage
                          endCursor
                        }
                      }
                    }
                """;
        };
    }

    private ShopifyPageResult<Order> mapOrderResponse(OrderGraphqlResponse response) {
        if (response == null || response.orders() == null)
            return new ShopifyPageResult<>(List.of(), new ShopifyPageResult.PageInfo(false, null));

        List<Order> orders = response.orders().edges().stream().map(edge -> {
            Order order = new Order();
            OrderEdgeNode node = edge.node();

            order.setShopifyId(node.id());
            order.setName(node.name());
            order.setShopifyCreatedAt(node.createdAt());

            // Map totalPriceSet.shopMoney
            if (node.totalPriceSet() != null && node.totalPriceSet().shopMoney() != null) {
                order.setTotalPrice(new BigDecimal(node.totalPriceSet().shopMoney().amount()));
                order.setCurrencyCode(node.totalPriceSet().shopMoney().currencyCode());
            }

            // Map financial and fulfillment status (using safe conversion)
            order.setFinancialStatus(FinancialStatus.fromString(node.displayFinancialStatus()));
            order.setFulfillmentStatus(FulfillmentStatus.fromString(node.displayFulfillmentStatus()));

            // Map customer info (can be null for guest checkouts)
            if (node.customer() != null) {
                order.setCustomerShopifyId(node.customer().id());
                order.setCustomerEmail(node.customer().email());
            }

            return order;
        }).collect(Collectors.toList());

        return new ShopifyPageResult<>(orders,
                new ShopifyPageResult.PageInfo(
                        response.orders().pageInfo().hasNextPage(),
                        response.orders().pageInfo().endCursor()
                )
        );
    }

    private ShopifyPageResult<Product> mapProductResponse(ProductGraphqlResponse response) {
        if (response == null || response.products() == null)
            return new ShopifyPageResult<>(List.of(), new ShopifyPageResult.PageInfo(false, null));

        List<Product> products = response.products().edges().stream().map(edge -> {
            Product product = new Product();
            ProductEdgeNode node = edge.node();

            product.setShopifyId(node.id());
            product.setTitle(node.title());
            product.setTotalInventory(node.totalInventory());
            product.setPublishedAt(node.publishedAt());
            product.setShopifyCreatedAt(node.createdAt());

            // Extract price from first variant
            if (node.variants() != null
                    && node.variants().edges() != null
                    && !node.variants().edges().isEmpty()
                    && node.variants().edges().getFirst().node() != null) {
                String priceStr = node.variants().edges().getFirst().node().price();
                if (priceStr != null) {
                    product.setPrice(new BigDecimal(priceStr));
                }
            }

            return product;
        }).collect(Collectors.toList());

        return new ShopifyPageResult<>(products,
                new ShopifyPageResult.PageInfo(
                        response.products().pageInfo().hasNextPage(),
                        response.products().pageInfo().endCursor()
                )
        );
    }

    private ShopifyPageResult<Customer> mapCustomerResponse(CustomerGraphqlResponse response) {
        if (response == null || response.customers() == null) {
            return new ShopifyPageResult<>(List.of(), new ShopifyPageResult.PageInfo(false, null));
        }

        List<Customer> customers = response.customers().edges().stream().map(edge -> {
            Customer customer = new Customer();
            CustomerEdgeNode node = edge.node();

            customer.setShopifyId(node.id());
            customer.setFirstName(node.firstName());
            customer.setLastName(node.lastName());
            customer.setEmail(node.email());
            customer.setShopifyCreatedAt(node.createdAt());
            customer.setOrdersCount(node.numberOfOrders());

            // Map amountSpent
            if (node.amountSpent() != null) {
                customer.setAmountSpent(new BigDecimal(node.amountSpent().amount()));
                customer.setAmountSpentCurrency(node.amountSpent().currencyCode());
            }

            // Map lastOrder (can be null)
            if (node.lastOrder() != null) {
                customer.setLastOrderCreatedAt(node.lastOrder().createdAt());
            }

            return customer;
        }).collect(Collectors.toList());

        return new ShopifyPageResult<>(customers,
                new ShopifyPageResult.PageInfo(
                        response.customers().pageInfo().hasNextPage(),
                        response.customers().pageInfo().endCursor()
                )
        );
    }
}