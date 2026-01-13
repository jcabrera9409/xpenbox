package org.xpenbox.auth.entity;

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
 * Token entity representing authentication tokens associated with users.
 */
@Entity
@Table(name = "tbl_token")
public class Token extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "resource_code", nullable = false, unique = true, length = 50)
    private String resourceCode;

    @Column(name = "access_token", nullable = false, length = 500)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken;

    @Column(name = "access_token_expires_at", nullable = false)
    private LocalDateTime accessTokenExpiresAt;

    @Column(name = "refresh_token_expires_at", nullable = true)
    private LocalDateTime refreshTokenExpiresAt;

    @Column(name = "persistent_session", nullable = false)
    private Boolean persistentSession = false;

    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    @Column(name = "issued_at", nullable = true)
    private LocalDateTime issuedAt;

    @Column(name = "last_used_at", nullable = true)
    private LocalDateTime lastUsedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public Boolean getPersistentSession() {
        return persistentSession;
    }

    public void setPersistentSession(Boolean persistentSession) {
        this.persistentSession = persistentSession;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
