package org.xpenbox.email.service;

import org.xpenbox.user.entity.User;

/**
 * Interface for email services, defining methods for sending various types of emails.
 * Implementations of this interface will handle the actual email sending logic, such as SMTP configuration and template rendering.
 */
public interface IEmailService {

    /**
     * Sends a verification email to the specified user with the provided verification link.
     * @param user The user to whom the verification email will be sent
     * @param verificationLink The URL that the user will click to verify their email address
     */
    void sendVerificationEmail(User user, String verificationLink);

    /**
     * Sends a password reset email to the specified user with the provided reset link.
     * @param user The user to whom the password reset email will be sent
     * @param resetLink The URL that the user will click to reset their password
     */
    void sendPasswordResetEmail(User user, String resetLink);

    /**
     * Sends a welcome email to the specified user with the provided login link.
     * @param user The user to whom the welcome email will be sent
     * @param loginLink The URL that the user will click to log in to their account
     */
    void sendWelcomeEmail(User user, String loginLink);
}
