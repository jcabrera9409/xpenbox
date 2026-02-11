package org.xpenbox.email.template.impl;

import java.util.Map;

import org.xpenbox.email.template.BaseEmailTemplate;

import io.quarkus.qute.Engine;

/**
 * Email template for password reset requests. This class provides the necessary data and template information
 * to render a password reset email using Qute templates.
 */
public class PasswordResetEmailTemplate extends BaseEmailTemplate {
    private final String userName;
    private final String resetLink;

    public PasswordResetEmailTemplate(Engine quteEngine, String userName, String resetLink) {
        super(quteEngine);
        this.userName = userName;
        this.resetLink = resetLink;
    }

    @Override
    public String getSubject() {
        return "Restablece tu contraseña de XpenBox";
    }

    @Override
    protected Map<String, Object> getTemplateModel() {
        return Map.of(
            "userName", userName,
            "resetLink", resetLink
        );
    }

    @Override
    protected String getTemplateName() {
        return "email/password-reset";
    }
    
}
