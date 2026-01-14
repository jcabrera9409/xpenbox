package org.xpenbox.creditcard.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.xpenbox.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * CreditCard entity representing credit card records associated with users.
 */
@Entity
@Table(name = "tbl_credit_card")
public class CreditCard extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "resource_code", nullable = false, unique = true, length = 100)
    private String resourceCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "credit_limit", nullable = false, precision = 13, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", nullable = false, precision = 13, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "state", nullable = false)
    private Boolean state = true;

    @Column(name = "billing_day", nullable = false)
    private Byte billingDay;

    @Column(name = "payment_day", nullable = false)
    private Byte paymentDay;

    @Column(name = "closing_date", nullable = true)
    private LocalDateTime closingDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Byte getBillingDay() {
        return billingDay;
    }

    public void setBillingDay(Byte billingDay) {
        this.billingDay = billingDay;
    }

    public Byte getPaymentDay() {
        return paymentDay;
    }

    public void setPaymentDay(Byte paymentDay) {
        this.paymentDay = paymentDay;
    }

    public LocalDateTime getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
