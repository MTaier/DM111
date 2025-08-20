package br.inatel.pos.dm111.vfu.api.core.interceptor;

import br.inatel.pos.dm111.vfu.api.core.ApiException;
import br.inatel.pos.dm111.vfu.api.core.AppErrorCode;
import br.inatel.pos.dm111.vfu.persistence.user.User;
import br.inatel.pos.dm111.vfu.persistence.user.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.impl.DefaultJws;
import io.jsonwebtoken.lang.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger((AuthenticationInterceptor.class));

    @Value("${vale-food.jwt.custom.issuer}")
    private String tokenIssuer;

    private final JwtParser jwtParser;
    private final UserRepository repository;

    public AuthenticationInterceptor(JwtParser jwtParser, UserRepository repository) {
        this.jwtParser = jwtParser;
        this.repository = repository;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        var method = req.getMethod();
        var uri = req.getRequestURI();

        if (!isJwtAuthRequired(method, uri)) {
            return true;
        }

        // JWT token validation
        var token = req.getHeader("Token");
        if (Strings.hasText(token)) {
            token = req.getHeader("token");
        }
        if (Strings.hasText(token)) {
            log.info("JWT token was not provided.");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        try {
            var jwt = (DefaultJws) jwtParser.parse(token);
            var payloadClaims = (Map<String, String>) jwt.getPayload();
            var issuer = payloadClaims.get("iss");
            var subject = payloadClaims.get("sub");
            var role = payloadClaims.get("role");
            var appJwtToken = new AppJwtToken(issuer, subject, role, method, uri);
            authenticateRequest(appJwtToken);
            return true;
        } catch (JwtException e) {
            log.error("Failure to validate the JWT token!", e);
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView modelAndView)
            throws Exception {
        log.debug("Request was processed successfully");
    }

    private void authenticateRequest(AppJwtToken appJwtToken) throws ApiException {
        if (!tokenIssuer.equals((appJwtToken.issuer()))) {
            log.info("Provided token issuer is not valid");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }
        var user = retrieveUserByEmail(appJwtToken.subject()).orElseThrow(() -> {
            log.info("User was not found for the provided subject");
            return new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        });

        if (!appJwtToken.role().equals(user.type().name())) {
            log.info("User type is invalid for the provided token role.");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        if (appJwtToken.uri().startsWith("/valefood/users/")) {
            var splitUri = appJwtToken.uri().split("/");
            var pathUserId = splitUri[3];
            if (!user.id().equals(pathUserId)) {
                log.info("User provided don't match to the User Id: {}", pathUserId);
                throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
            }
        }

        if (appJwtToken.uri().startsWith("/valefood/users")) {
            log.info("Read all Users not supported");
            throw new ApiException(AppErrorCode.OPERATION_NOT_SUPPORTED);
        }
    }

    private Optional<User> retrieveUserByEmail(String email) throws ApiException {
        try {
            return repository.getByEmail(email);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read an users from DB by email.", e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private boolean isJwtAuthRequired(String method, String uri) {
        if (uri.equals("/valefood/users")) {
            return !HttpMethod.POST.name().equals(method);
        }
        return true;
    }
}
