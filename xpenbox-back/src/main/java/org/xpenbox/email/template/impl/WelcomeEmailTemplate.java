package org.xpenbox.email.template.impl;

import java.util.Map;

import org.xpenbox.email.template.BaseEmailTemplate;

import io.quarkus.qute.Engine;

/**
 * Email template for welcoming new users. This class provides the necessary data and template information
 * to render a welcome email using Qute templates.
 */
public class WelcomeEmailTemplate extends BaseEmailTemplate {
    private final String userName;
    private final String loginLink;

    public WelcomeEmailTemplate(Engine quteEngine, String userName, String loginLink) {
        super(quteEngine);
        this.userName = userName;
        this.loginLink = loginLink;
    }

    @Override
    public String getSubject() {
        return "¡Bienvenido a Xpenbox!";
    }

    @Override
    protected Map<String, Object> getTemplateModel() {
        return Map.of(
            "userName", userName,
            "loginLink", loginLink
        );
    }

    @Override
    protected String getTemplateName() {
        return "email/welcome-email";
    }
    
}
