package tech.thedumbdev.shop_lens.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String shopifyId;

    @Column(nullable = false)
    private String title;

    private Integer totalInventory;

    private String publishedAt;

    private String shopifyCreatedAt;

    // Price from first variant
    @Column(precision = 19, scale = 4)
    private BigDecimal price;
}
