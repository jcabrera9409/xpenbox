package org.xpenbox.user.service;

/**
 * Service interface for managing user tokens, such as email verification and password reset tokens.
 */
public interface IUserTokenService {

    /**
     * Verifies the email token and marks the user's email as verified if the token is valid.
     * @param token the email verification token to be verified
     */
    void verifyEmailToken(String token);

    /**
     * Generates a token for email verification or password reset and associates it with the user's email.
     * @param email the email of the user for whom the token is being generated
     */
    void generateEmailVerificationToken(String email);

    /**
     * Verifies the password reset token and resets the user's password if the token is valid.
     * @param token the password reset token to be verified
     * @param newPassword the new password to be set for the user
     */
    void verifyAndResetPasswordWithToken(String token, String newPassword);

    /**
     * Generates a token for password reset and associates it with the user's email.
     * @param email the email of the user for whom the token is being generated
     */
    void generatePasswordResetToken(String email);
}
