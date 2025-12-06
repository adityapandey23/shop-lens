package tech.thedumbdev.shop_lens.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.thedumbdev.shop_lens.model.enums.FinancialStatus;
import tech.thedumbdev.shop_lens.model.enums.FulfillmentStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String shopifyId;

    // Order name/number (e.g., "#1001")
    private String name;

    private String shopifyCreatedAt;

    // From totalPriceSet.shopMoney
    @Column(precision = 19, scale = 4)
    private BigDecimal totalPrice;

    private String currencyCode;

    // e.g., "PAID", "PENDING", "REFUNDED"
    private FinancialStatus financialStatus;

    // e.g., "FULFILLED", "UNFULFILLED", "PARTIALLY_FULFILLED"
    private FulfillmentStatus fulfillmentStatus;

    // Customer info (denormalized for quick access)
    private String customerShopifyId;

    private String customerEmail;
}
