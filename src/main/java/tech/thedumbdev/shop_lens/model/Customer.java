package tech.thedumbdev.shop_lens.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "customers")
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String shopifyId;

    private String firstName;

    private String lastName;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    private String shopifyCreatedAt;

    // Additional fields from GraphQL response
    private Integer ordersCount;

    @Column(precision = 19, scale = 4)
    private BigDecimal amountSpent;

    private String amountSpentCurrency;

    private String lastOrderCreatedAt;
}
