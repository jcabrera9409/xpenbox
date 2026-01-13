package org.xpenbox.income.entity;

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
 * Income entity representing income records associated with users.
 */
@Entity
@Table(name = "tbl_income")
public class Income extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "resource_code", nullable = false, unique = true, length = 50)
    private String resourceCode;

    @Column(name = "concept", nullable = false, length = 150)
    private String concept;

    @Column(name = "income_date", nullable = false)
    private LocalDateTime incomeDate;

    @Column(name = "total_amount", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public LocalDateTime getIncomeDate() {
        return incomeDate;
    }

    public void setIncomeDate(LocalDateTime incomeDate) {
        this.incomeDate = incomeDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
