package org.xpenbox.email.template.impl;

import java.util.Map;

import org.xpenbox.email.template.BaseEmailTemplate;

import io.quarkus.qute.Engine;

/**
 * Email template for user email verification. This class provides the necessary data and template information
 * to render a verification email using Qute templates.
 */
public class VerifyEmailTemplate extends BaseEmailTemplate {
    private final String userName;
    private final String verificationLink;

    public VerifyEmailTemplate(Engine quteEngine, String userName, String verificationLink) {
        super(quteEngine);
        this.userName = userName;
        this.verificationLink = verificationLink;
    }

    @Override
    protected String getTemplateName() {
        return "email/verify-email";
    }

    @Override
    protected Map<String, Object> getTemplateModel() {
        return Map.of(
            "userName", userName,
            "verificationLink", verificationLink
        );
    }

    @Override
    public String getSubject() {
        return "Verifica tu correo electrónico";
    }
    
   
}