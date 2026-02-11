package org.xpenbox.authorization.dto;

/**
 * Data Transfer Object for password reset requests, containing the token and the new password.
 * @param token the password reset token to be verified
 * @param newPassword the new password to be set for the user
 */
public record ResetPasswordRequestDTO (
    String token,
    String newPassword
) { }
