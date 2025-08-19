package br.inatel.pos.dm111.vfr.api.core.interceptor;

import br.inatel.pos.dm111.vfr.api.core.ApiException;
import br.inatel.pos.dm111.vfr.api.core.AppErrorCode;
import br.inatel.pos.dm111.vfr.persistence.restaurant.Restaurant;
import br.inatel.pos.dm111.vfr.persistence.restaurant.RestaurantRepository;
import br.inatel.pos.dm111.vfr.persistence.user.User;
import br.inatel.pos.dm111.vfr.persistence.user.UserRepository;
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

    private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Value("${vale-food.jwt.custom.issuer}")
    private String tokenIssuer;

    private final JwtParser jwtParser;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public AuthenticationInterceptor(JwtParser jwtParser, UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.jwtParser = jwtParser;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    // USERS
    // CREATE - JWT auth not required
    // UPDATE, DELETE, READ - PROTECTED API
    // RESTAURANTS
    // CREATE, UPDATE, DELETE, READ - PROTECTED API
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var method = request.getMethod();
        var uri = request.getRequestURI();

        // JWT token validation
        var token = request.getHeader("Token");
        if (Strings.hasText(token)) {
            token = request.getHeader("token");
        }

        if (!Strings.hasLength(token)) {
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
            log.error("Failure to validate the JWT token.", e);
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
       log.debug("Request was processed successfully");
    }

    private void authenticateRequest(AppJwtToken appJwtToken) throws ApiException {
        if (!tokenIssuer.equals(appJwtToken.issuer())) {
            log.info("Provided token issuer is not valid");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        var user = retrieveUserByEmail(appJwtToken.subject()).orElseThrow(() -> {
            log.info("User was not found for the provided token subject.");
            return new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        });

        if (!appJwtToken.role().equals(user.type().name())) {
            log.info("User type is invalid for the provided token role.");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        if (appJwtToken.uri().startsWith("/valefood/restaurants/")) {
            if (appJwtToken.method().equals(HttpMethod.PUT.name()) ||
                    appJwtToken.method().equals(HttpMethod.DELETE.name()))  {
                var splitUri = appJwtToken.uri().split("/");
                var pathRestaurantId = splitUri[3];
                var restaurant = retrieveRestaurantById(pathRestaurantId).orElseThrow(() -> {
                    log.info("Restaurant does not exist");
                    return new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
                });

                if (!user.id().equals(restaurant.userId())) {
                    log.info("User provided didn't match to the user linked to restaurant user Id: {}", user.id());
                    throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
                }
            }
        }
    }

    private Optional<User> retrieveUserByEmail(String email) throws ApiException {
        try {
            return userRepository.getByEmail(email);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read an users from DB by email.", e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private Optional<Restaurant> retrieveRestaurantById(String id) throws ApiException {
        try {
            return restaurantRepository.getById(id);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read a restaurant from DB by id {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }
}
