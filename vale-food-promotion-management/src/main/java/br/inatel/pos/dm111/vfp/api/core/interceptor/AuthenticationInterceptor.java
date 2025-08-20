package br.inatel.pos.dm111.vfp.api.core.interceptor;

import br.inatel.pos.dm111.vfp.api.core.ApiException;
import br.inatel.pos.dm111.vfp.api.core.AppErrorCode;
import br.inatel.pos.dm111.vfp.persistence.promotion.Promotion;
import br.inatel.pos.dm111.vfp.persistence.promotion.PromotionRepository;
import br.inatel.pos.dm111.vfp.persistence.restaurant.Restaurant;
import br.inatel.pos.dm111.vfp.persistence.restaurant.RestaurantRepository;
import br.inatel.pos.dm111.vfp.persistence.user.User;
import br.inatel.pos.dm111.vfp.persistence.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
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
    private final PromotionRepository promotionRepository;

    public AuthenticationInterceptor(JwtParser jwtParser,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            PromotionRepository promotionRepository) {
        this.jwtParser = jwtParser;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.promotionRepository = promotionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var method = request.getMethod();
        var uri = request.getRequestURI();

        // Resolve token from Authorization or Token headers
        String token = resolveToken(request);
        if (!Strings.hasLength(token)) {
            log.info("JWT token was not provided.");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        try {
            // Parse and validate the signed claims using the configured JwtParser.
            var jws = jwtParser.parseSignedClaims(token);
            Claims claims = jws.getPayload();
            String issuer = claims.getIssuer();
            String subject = claims.getSubject();
            Object roleObj = claims.get("role");
            String role = roleObj != null ? roleObj.toString() : null;

            var appJwtToken = new AppJwtToken(issuer, subject, role, method, uri);
            authenticateRequest(appJwtToken);

            return true;

        } catch (JwtException e) {
            log.error("Failure to validate the JWT token.", e);
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        log.debug("Request was processed successfully");
    }

    private void authenticateRequest(AppJwtToken token) throws ApiException {
        // Validate issuer
        if (!tokenIssuer.equals(token.issuer())) {
            log.info("Provided token issuer is not valid");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        // Retrieve user by email (subject)
        var user = retrieveUserByEmail(token.subject()).orElseThrow(() -> {
            log.info("User was not found for the provided token subject.");
            return new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        });

        // Validate role matches user type
        if (!token.role().equals(user.type().name())) {
            log.info("User type is invalid for the provided token role.");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        // Authorization checks for promotion endpoints
        if (token.uri().startsWith("/valefood/promotions")) {
            // Only restaurant users can perform non-GET operations
            if (!User.UserType.RESTAURANT.equals(user.type()) && !token.method().equals(HttpMethod.GET.name())) {
                log.info("User is not authorized to manage promotions.");
                throw new ApiException(AppErrorCode.INVALID_USER_TYPE);
            }

            // For update/delete operations verify ownership
            if (token.method().equals(HttpMethod.PUT.name()) || token.method().equals(HttpMethod.DELETE.name())) {
                var split = token.uri().split("/");
                if (split.length > 3) {
                    var promotionId = split[3];
                    var promotion = retrievePromotionById(promotionId).orElseThrow(() -> {
                        log.info("Promotion does not exist");
                        return new ApiException(AppErrorCode.PROMOTION_NOT_FOUND);
                    });
                    var restaurant = retrieveRestaurantById(promotion.restaurantId()).orElseThrow(() -> {
                        log.info("Restaurant does not exist");
                        return new ApiException(AppErrorCode.RESTAURANT_NOT_FOUND);
                    });
                    if (!user.id().equals(restaurant.userId())) {
                        log.info("User provided didn't match to the user linked to restaurant user Id: {}", user.id());
                        throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
                    }
                }
            }
        }
    }

    private Optional<User> retrieveUserByEmail(String email) throws ApiException {
        try {
            return userRepository.getByEmail(email);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read a user from DB by email.", e);
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

    private Optional<Promotion> retrievePromotionById(String id) throws ApiException {
        try {
            return promotionRepository.getById(id);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read a promotion from DB by id {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (Strings.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        String tokenHeader = request.getHeader("Token");
        if (Strings.hasText(tokenHeader)) {
            return tokenHeader;
        }
        return request.getHeader("token");
    }
}