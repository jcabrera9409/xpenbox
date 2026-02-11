package org.xpenbox.email.template;

import org.xpenbox.email.template.impl.PasswordResetEmailTemplate;
import org.xpenbox.email.template.impl.VerifyEmailTemplate;
import org.xpenbox.email.template.impl.WelcomeEmailTemplate;
import org.xpenbox.user.entity.User;

import io.quarkus.qute.Engine;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory for creating email templates with user-specific data.
 * This centralizes template creation and allows for easy extension with new email types.
 */
@ApplicationScoped
public class EmailTemplateFactory {
    
    private final Engine quteEngine;

    public EmailTemplateFactory(Engine quteEngine) {
        this.quteEngine = quteEngine;
    }

    /**
     * Creates a verification email template with user data
     * @param user The user to verify
     * @param verificationLink The verification URL
     * @return Configured email template
     */
    public VerifyEmailTemplate createVerifyEmailTemplate(User user, String verificationLink) {
        String userName = user.getEmail().split("@")[0];
        return new VerifyEmailTemplate(quteEngine, userName, verificationLink);
    }

    /**
     * Creates a password reset email template with user data
     * @param user The user to reset password for
     * @param resetLink The password reset URL
     * @return Configured email template
     */
    public PasswordResetEmailTemplate createPasswordResetEmailTemplate(User user, String resetLink) {
        String userName = user.getEmail().split("@")[0];
        return new PasswordResetEmailTemplate(quteEngine, userName, resetLink);
    }

    /**
     * Creates a welcome email template with user data
     * @param user The user to welcome
     * @param loginLink The login URL
     * @return Configured email template
     */
    public WelcomeEmailTemplate createWelcomeEmailTemplate(User user, String loginLink) {
        String userName = user.getEmail().split("@")[0];
        return new WelcomeEmailTemplate(quteEngine, userName, loginLink);
    }
}
