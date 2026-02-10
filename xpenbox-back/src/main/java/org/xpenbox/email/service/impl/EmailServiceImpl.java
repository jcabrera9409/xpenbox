package org.xpenbox.email.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.email.client.EmailApiClient;
import org.xpenbox.email.service.IEmailService;
import org.xpenbox.email.template.EmailTemplateFactory;
import org.xpenbox.email.template.impl.VerifyEmailTemplate;
import org.xpenbox.email.template.impl.WelcomeEmailTemplate;
import org.xpenbox.user.entity.User;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailServiceImpl implements IEmailService {
    private static final Logger LOG = Logger.getLogger(EmailServiceImpl.class);

    private final EmailApiClient emailApiClient;
    private final EmailTemplateFactory emailTemplateFactory;

    public EmailServiceImpl(EmailApiClient emailApiClient, EmailTemplateFactory emailTemplateFactory) {
        this.emailApiClient = emailApiClient;
        this.emailTemplateFactory = emailTemplateFactory;
    }

    @Override
    public void sendVerificationEmail(User user, String verificationLink) {
        LOG.infof("Preparing to send verification email to user: %s", user.getEmail());
        VerifyEmailTemplate template = emailTemplateFactory.createVerifyEmailTemplate(user, verificationLink);

        emailApiClient.sendEmail(user.getEmail(), template.getSubject(), template.getContent());
        LOG.infof("Verification email sent to user: %s", user.getEmail());
    }

    @Override
    public void sendWelcomeEmail(User user, String loginLink) {
        LOG.infof("Preparing to send welcome email to user: %s", user.getEmail());
        WelcomeEmailTemplate template = emailTemplateFactory.createWelcomeEmailTemplate(user, loginLink);

        emailApiClient.sendEmail(user.getEmail(), template.getSubject(), template.getContent());
        LOG.infof("Welcome email sent to user: %s", user.getEmail());
    }
    
}
