package org.xpenbox.email.template;

import org.xpenbox.email.template.impl.VerifyEmailTemplate;
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
}
