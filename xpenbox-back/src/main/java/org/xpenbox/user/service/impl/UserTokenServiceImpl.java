package org.xpenbox.user.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.xpenbox.email.service.IEmailService;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.entity.UserToken;
import org.xpenbox.user.entity.UserToken.UserTokenType;
import org.xpenbox.user.repository.UserRepository;
import org.xpenbox.user.repository.UserTokenRepository;
import org.xpenbox.user.service.IUserTokenService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of IUserTokenService for managing user tokens such as email verification and password reset tokens.
 */
@ApplicationScoped
public class UserTokenServiceImpl implements IUserTokenService {
    private static final Logger LOG = Logger.getLogger(UserTokenServiceImpl.class);

    @ConfigProperty(name = "email.token.expiration")
    private Long emailTokenExpiration;

    @ConfigProperty(name = "email.token.verification.url")
    private String emailTokenVerificationUrl;

    @ConfigProperty(name = "password.reset.token.verification.url")
    private String passwordResetTokenVerificationUrl;

    @ConfigProperty(name = "email.token.login.url")
    private String emailTokenLoginUrl;

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final IEmailService emailService;

    public UserTokenServiceImpl(
        UserRepository userRepository,
        UserTokenRepository userTokenRepository,
        IEmailService emailService
    ) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public void verifyEmailToken(String token) {
        LOG.infof("Verifying email token: %s", token);
        UserToken userToken = userTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                LOG.warnf("Invalid email verification token: %s", token);
                return new BadRequestException("Invalid email verification token");
            });

        if (userToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            LOG.warnf("Email verification token expired: %s", token);
            throw new BadRequestException("Email verification token has expired");
        }

        if (userToken.getUser().getVerified()) {
            LOG.warnf("Email already verified for token: %s", token);
            throw new BadRequestException("Email is already verified");
        }

        if (userToken.getUsed()) {
            LOG.warnf("Email verification token already used: %s", token);
            throw new BadRequestException("Email verification token has already been used");
        }

        User user = userToken.getUser();
        user.setVerified(true);
        userRepository.persist(user);

        userTokenRepository.delete(userToken);

        this.emailService.sendWelcomeEmail(user, emailTokenLoginUrl);
        LOG.infof("Email verified successfully for user: %s", user.getEmail());
    }

    @Override
    public void generateEmailVerificationToken(String email) {
        LOG.infof("Generating email verification token for email: %s", email);
        User user = validateAndGetUser(email);

        if (user == null) {
            LOG.warnf("User with email %s not found, cannot generate email verification token", email);
            return;
        }

        if (user.getVerified()) {
            LOG.warnf("Email %s is already verified", email);
            return;
        }

        UserToken existingToken = userTokenRepository.findActiveTokenByUserIdAndType(user.id, UserTokenType.EMAIL_VERIFICATION);
        if (existingToken != null) {
            LOG.warnf("An active email verification token already exists for email: %s", email);
            return;
        }

        userTokenRepository.deleteByUserIdAndUserTokenType(user.id, UserTokenType.EMAIL_VERIFICATION);

        UserToken token = generateEmailVerificationToken(user);
        String verificationLink = String.format("%s?token=%s", emailTokenVerificationUrl, token.getToken());

        this.emailService.sendVerificationEmail(user, verificationLink);
        LOG.infof("Email verification token generated and email sent to: %s", email);
    }

    @Override
    public void verifyAndResetPasswordWithToken(String token, String newPassword) {
        LOG.infof("Verifying password reset token: %s", token);
        UserToken userToken = userTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                LOG.warnf("Invalid password reset token: %s", token);
                return new BadRequestException("Cannot reset password. Please verify your email first.");
            });

        if (userToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            LOG.warnf("Password reset token expired: %s", token);
            throw new BadRequestException("Cannot reset password. Please verify your email first.");
        }

        if (!userToken.getUser().getVerified()) {
            LOG.warnf("Email %s is not verified, cannot reset password", userToken.getUser().getEmail());
            throw new BadRequestException("Cannot reset password. Please verify your email first.");
        }

        if (userToken.getUsed()) {
            LOG.warnf("Password reset token already used: %s", token);
            throw new BadRequestException("Cannot reset password. Please verify your email first.");
        }

        User user = userToken.getUser();
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));

        userRepository.persist(user);
        userTokenRepository.delete(userToken);

        LOG.infof("Password reset successfully for user: %s", user.getEmail());
    }

    @Override
    public void generatePasswordResetToken(String email) {
        LOG.infof("Generating password reset token for email: %s", email);
        User user = validateAndGetUser(email);

        if (user == null) {
            LOG.warnf("User with email %s not found, cannot generate password reset token", email);
            return;
        }

        if (!user.getVerified()) {
            LOG.warnf("Email %s is not verified, cannot generate password reset token", email);
            return;
        }

        UserToken existingToken = userTokenRepository.findActiveTokenByUserIdAndType(user.id, UserTokenType.PASSWORD_RESET);
        if (existingToken != null) {
            LOG.warnf("An active password reset token already exists for email: %s", email);
            return;
        }

        userTokenRepository.deleteByUserIdAndUserTokenType(user.id, UserTokenType.PASSWORD_RESET);

        UserToken token = generatePasswordResetToken(user);
        String resetLink = String.format("%s?token=%s", passwordResetTokenVerificationUrl, token.getToken());

        this.emailService.sendPasswordResetEmail(user, resetLink);
        LOG.infof("Password reset token generated and email sent to: %s", email);
    }

    private User validateAndGetUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private UserToken generateEmailVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(emailTokenExpiration / 1000);
        UserToken userToken = UserToken.generateEmailVerificationToken(user, token, expiresAt);

        userTokenRepository.persist(userToken);
        LOG.infof("Email verification token generated and saved for user: %s", user.getEmail());

        return userToken;
    }

    private UserToken generatePasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(emailTokenExpiration / 1000);
        UserToken userToken = UserToken.generatePasswordResetToken(user, token, expiresAt);

        userTokenRepository.persist(userToken);
        LOG.infof("Password reset token generated and saved for user: %s", user.getEmail());

        return userToken;
    }

}
