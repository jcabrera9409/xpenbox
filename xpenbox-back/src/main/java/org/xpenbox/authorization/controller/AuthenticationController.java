package org.xpenbox.authorization.controller;

import org.jboss.logging.Logger;
import org.xpenbox.authorization.dto.LoginRequestDTO;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.service.IAuthenticationService;
import org.xpenbox.authorization.service.ITokenService;
import org.xpenbox.user.dto.UserCreateDTO;
import org.xpenbox.user.service.IUserService;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationController {
    private static final Logger LOG = Logger.getLogger(AuthenticationController.class);

    private final IUserService userService;
    private final ITokenService tokenService;
    private final IAuthenticationService authenticationService;

    public AuthenticationController(
        IUserService userService,
        ITokenService tokenService,
        IAuthenticationService authenticationService
    ) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationService = authenticationService;
    }

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

    @POST
    @Path("/refresh")
    @Authenticated
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

    @POST
    @Path("/logout")
    @Authenticated
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

    @SuppressWarnings("deprecation")
    private NewCookie accessCookie(TokenResponseDTO token) { 
        LOG.infof("Creating access cookie with expiration: %d", token.accessTokenExpiresIn());
        return new NewCookie(
            "access_token", 
            token.accessToken(), 
            "/api/rest/v1", 
            null, 
            null, 
            (int) (token.accessTokenExpiresIn() == null ? -1 : token.accessTokenExpiresIn().longValue()), 
            false, 
            true
        );
    }

    @SuppressWarnings("deprecation")
    private NewCookie refreshCookie(TokenResponseDTO token) { 
        LOG.infof("Creating refresh cookie with expiration: %d", token.refreshTokenExpiresIn());
        return new NewCookie(
            "refresh_token", 
            token.refreshToken(), 
            "/api/rest/v1", 
            null, 
            null, 
            (int) (token.refreshTokenExpiresIn() == null ? -1 : token.refreshTokenExpiresIn().longValue()), 
            false, 
            true
        );
    }

    @SuppressWarnings("deprecation")
    private NewCookie expire(String name) {
        LOG.infof("Expiring cookie: %s", name);
        return new NewCookie(name, "", "/api/rest/v1", null, null, -1, false, true);
    }
}
