package org.xpenbox.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.xpenbox.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Subscription entity representing user subscriptions to plans.
 */
@Entity
@Table(name = "tbl_subscription")
public class Subscription extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "resource_code", nullable = false, unique = true, length = 100)
    private String resourceCode;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_price", nullable = false, precision = 13, scale = 2)
    private BigDecimal planPrice;

    @Column(name = "plan_currency", nullable = false, length = 3)
    private String planCurrency;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDateTime endDate;

    @Column(name = "next_billing_date", nullable = true)
    private LocalDateTime nextBillingDate;

    @Column(name = "provider", nullable = true, length = 30)
    private String provider;

    @Column(name = "provider_subscription_id", nullable = true, length = 100)
    private String providerSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    // Public enum for subscription status
    public enum SubscriptionStatus {
        PENDING,
        ACTIVE,
        PAST_DUE,
        CANCELLED,
        EXPIRED,
        TRIAL
    }

    // Getters and setters
    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getPlanPrice() {
        return planPrice;
    }

    public void setPlanPrice(BigDecimal planPrice) {
        this.planPrice = planPrice;
    }

    public String getPlanCurrency() {
        return planCurrency;
    }

    public void setPlanCurrency(String planCurrency) {
        this.planCurrency = planCurrency;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDateTime nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderSubscriptionId() {
        return providerSubscriptionId;
    }

    public void setProviderSubscriptionId(String providerSubscriptionId) {
        this.providerSubscriptionId = providerSubscriptionId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }
}
