package org.xpenbox.user.entity;

import java.time.LocalDateTime;

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
 * UserToken entity representing tokens associated with users for actions like email verification and password reset.
 */
@Entity
@Table(name = "tbl_user_token")
public class UserToken extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "token", nullable = false, length = 250)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private UserTokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private Boolean used = false;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum UserTokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    private UserToken() {
        // Default constructor for JPA
    }

    public static UserToken generateEmailVerificationToken(User user, String token, LocalDateTime expiresAt) {
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(token);
        userToken.setTokenType(UserTokenType.EMAIL_VERIFICATION);
        userToken.setExpiresAt(expiresAt);
        userToken.setUsed(false);
        return userToken;
    }

    public static UserToken generatePasswordResetToken(User user, String token, LocalDateTime expiresAt) {
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(token);
        userToken.setTokenType(UserTokenType.PASSWORD_RESET);
        userToken.setExpiresAt(expiresAt);
        userToken.setUsed(false);
        return userToken;
    }

    // Getters and Setters
    public void setToken(String token) {
        this.token = token;
    } 

    public String getToken() {
        return token;
    }

    public void setTokenType(UserTokenType tokenType) {
        this.tokenType = tokenType;
    }

    public UserTokenType getTokenType() {
        return tokenType;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
