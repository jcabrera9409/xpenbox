package org.xpenbox.authorization.controller;

import org.jboss.logging.Logger;
import org.xpenbox.authorization.dto.LoginRequestDTO;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.dto.VerifyEmailRequestDTO;
import org.xpenbox.authorization.service.IAuthenticationService;
import org.xpenbox.authorization.service.ITokenService;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.user.dto.UserCreateDTO;
import org.xpenbox.user.service.IUserService;
import org.xpenbox.user.service.IUserTokenService;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

/**
 * AuthenticationController handles HTTP requests related to user authentication.
 * It provides endpoints for user registration, login, token refresh, and logout.
 */
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationController {
    private static final Logger LOG = Logger.getLogger(AuthenticationController.class);

    private final IUserService userService;
    private final ITokenService tokenService;
    private final IAuthenticationService authenticationService;
    private final IUserTokenService userTokenService;

    public AuthenticationController(
        IUserService userService,
        ITokenService tokenService,
        IAuthenticationService authenticationService,
        IUserTokenService userTokenService
    ) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationService = authenticationService;
        this.userTokenService = userTokenService;
    }

    /**
     * Register a new user
     * @param userRequest User creation data transfer object
     * @return Response indicating the result of the registration
     */
    @POST
    @Path("/register")
    @PermitAll
    @Transactional
    public Response register(@Valid UserCreateDTO userRequest) {
        LOG.infof("Register request received for email: %s", userRequest.email());
        userService.register(userRequest);
        LOG.infof("Register successful for email: %s", userRequest.email());
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Verify email using the provided token
     * @param token Email verification token from query parameter
     * @return Response indicating the result of the email verification
     */
    @GET
    @Path("/verify-email")
    @PermitAll
    @Transactional
    public Response verifyEmail(@Valid @QueryParam("token") String token) {
        LOG.infof("Email verification request received with token: %s", token);

        userTokenService.verifyEmailToken(token);

        LOG.infof("Email verification successful for token: %s", token);

        return Response.ok(
            APIResponseDTO.success(
                "Email verified successfully. You can now log in.", 
                null, 
                Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Resend email verification token to the user's email address.
     * @param verifyEmailRequest Request data transfer object containing the email to resend the verification token to
     * @return Response indicating the result of the resend operation
     */
    @POST
    @Path("/verify-email/resend")
    @PermitAll
    @Transactional
    public Response resendVerifyEmail(@Valid VerifyEmailRequestDTO verifyEmailRequest) {
        LOG.infof("Resend email verification request received for email: %s", verifyEmailRequest.email());

        userTokenService.generateEmailVerificationToken(verifyEmailRequest.email());

        LOG.infof("Resend email verification successful for email: %s", verifyEmailRequest.email());

        return Response.noContent().build();
    }

    /**
     * Login a user and issue tokens
     * @param loginRequest Login request data transfer object
     * @return Response containing the issued tokens in cookies
     */
    @POST
    @Path("/login")
    @PermitAll
    @Transactional
    public Response login(@Valid LoginRequestDTO loginRequest) {
        LOG.infof("Login request received for email: %s", loginRequest.email());
        TokenResponseDTO token = authenticationService.login(loginRequest.email(), loginRequest.password(), loginRequest.rememberMe());

        LOG.infof("Login successful for email: %s", loginRequest.email());
        return Response.ok()
            .cookie(accessCookie(token))
            .cookie(refreshCookie(token))
            .build();
    }

    /**
     * Refresh access and refresh tokens
     * @param refreshToken Refresh token from cookie
     * @return Response containing the new tokens in cookies
     */
    @POST
    @Path("/refresh")
    @PermitAll
    @Transactional
    public Response refresh(@CookieParam("refresh_token") String refreshToken) {
        LOG.infof("Token refresh request received");
        TokenResponseDTO token = tokenService.refreshToken(refreshToken);
        LOG.infof("Token refresh successful");
        return Response.ok()
            .cookie(accessCookie(token))
            .cookie(refreshCookie(token))
            .build();
    }

    /**
     * Logout a user by revoking tokens
     * @param refreshToken Refresh token from cookie
     * @return Response indicating the result of the logout
     */
    @POST
    @Path("/logout")
    @PermitAll
    @Transactional
    public Response logout(@CookieParam("refresh_token") String refreshToken) {
        LOG.infof("Logout request received");
        tokenService.revokeToken(refreshToken);
        LOG.infof("Logout successful");
        return Response.ok()
            .cookie(expire("access_token"))
            .cookie(expire("refresh_token"))
            .build();
    }

    @GET
    @Authenticated
    @Path("/check")
    public Response check() {
        LOG.infof("Session check request received");
        return Response.ok().build();
    }

    @SuppressWarnings("deprecation")
    private NewCookie accessCookie(TokenResponseDTO token) { 
        LOG.infof("Creating access cookie with expiration: %d", token.accessTokenExpiresIn());
        return new NewCookie(
            "access_token", 
            token.accessToken(), 
            "/", // Cambiar a / para que sea accesible desde todas las rutas
            null, 
            null, 
            (int) token.accessTokenExpiresIn().longValue(), 
            false, // Secure=false para desarrollo (cambiar a true en producción con HTTPS)
            true
        );
    }

    @SuppressWarnings("deprecation")
    private NewCookie refreshCookie(TokenResponseDTO token) { 
        LOG.infof("Creating refresh cookie with expiration: %d", token.refreshTokenExpiresIn());
        return new NewCookie(
            "refresh_token", 
            token.refreshToken(), 
            "/", // Cambiar a / para que sea accesible desde todas las rutas
            null, 
            null, 
            (int) token.refreshTokenExpiresIn().longValue(), 
            false, // Secure=false para desarrollo (cambiar a true en producción con HTTPS)
            true
        );
    }

    @SuppressWarnings("deprecation")
    private NewCookie expire(String name) {
        LOG.infof("Expiring cookie: %s", name);
        return new NewCookie(name, "", "/", null, null, 0, false, true);
    }
}
