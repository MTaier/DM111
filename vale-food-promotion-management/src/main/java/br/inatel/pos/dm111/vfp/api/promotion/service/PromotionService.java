package br.inatel.pos.dm111.vfp.api.promotion.service;

import br.inatel.pos.dm111.vfp.api.core.ApiException;
import br.inatel.pos.dm111.vfp.api.core.AppErrorCode;
import br.inatel.pos.dm111.vfp.api.promotion.PromotionRequest;
import br.inatel.pos.dm111.vfp.api.promotion.PromotionResponse;
import br.inatel.pos.dm111.vfp.persistence.promotion.Promotion;
import br.inatel.pos.dm111.vfp.persistence.promotion.PromotionRepository;
import br.inatel.pos.dm111.vfp.persistence.restaurant.Restaurant;
import br.inatel.pos.dm111.vfp.persistence.restaurant.RestaurantRepository;
import br.inatel.pos.dm111.vfp.persistence.user.User;
import br.inatel.pos.dm111.vfp.persistence.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public PromotionService(PromotionRepository promotionRepository,
            RestaurantRepository restaurantRepository,
            UserRepository userRepository) {
        this.promotionRepository = promotionRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public List<PromotionResponse> searchPromotions() throws ApiException {
        try {
            return promotionRepository.getAll().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch promotions.", e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    public List<PromotionResponse> searchPromotionsByRestaurant(String restaurantId) throws ApiException {
        try {
            return promotionRepository.getByRestaurantId(restaurantId).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch promotions by restaurant {}.", restaurantId, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    public PromotionResponse searchPromotion(String id) throws ApiException {
        return retrievePromotionById(id)
                .map(this::toResponse)
                .orElseThrow(() -> {
                    log.warn("Promotion was not found. Id: {}", id);
                    return new ApiException(AppErrorCode.PROMOTION_NOT_FOUND);
                });
    }

    public PromotionResponse createPromotion(PromotionRequest request) throws ApiException {
        validateRestaurant(request.restaurantId());
        var promotion = buildPromotion(request, null);
        promotionRepository.save(promotion);
        log.info("Promotion was successfully created. Id: {}", promotion.id());
        return toResponse(promotion);
    }

    public PromotionResponse updatePromotion(PromotionRequest request, String id) throws ApiException {
        // ensure the promotion exists
        var existing = retrievePromotionById(id)
                .orElseThrow(() -> {
                    log.warn("Promotion was not found. Id: {}", id);
                    return new ApiException(AppErrorCode.PROMOTION_NOT_FOUND);
                });
        // validate the restaurant id provided in the request
        validateRestaurant(request.restaurantId());
        var updated = buildPromotion(request, existing.id());
        promotionRepository.save(updated);
        log.info("Promotion was successfully updated. Id: {}", updated.id());
        return toResponse(updated);
    }

    public void removePromotion(String id) throws ApiException {
        try {
            var existing = promotionRepository.getById(id);
            if (existing.isPresent()) {
                promotionRepository.delete(id);
                log.info("Promotion was successfully removed. Id: {}", id);
            } else {
                log.info("Promotion id {} not found; delete request ignored.", id);
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to delete promotion {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    public List<PromotionResponse> searchPromotionsByUserPreferences(String userId) throws ApiException {
        var userOpt = retrieveUserById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found. Id: {}", userId);
            throw new ApiException(AppErrorCode.USER_NOT_FOUND);
        }
        var user = userOpt.get();
        var prefs = user.preferredCategories();
        if (prefs == null || prefs.isEmpty()) {
            return List.of();
        }
        var lower = prefs.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        try {
            return promotionRepository.getAll().stream()
                    .filter(p -> lower.contains(p.category().toLowerCase(Locale.ROOT)))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch promotions for user {}", userId, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private Promotion buildPromotion(PromotionRequest request, String overrideId) {
        var promotionId = (overrideId != null && !overrideId.isEmpty())
                ? overrideId
                : (request.id() != null && !request.id().isEmpty()
                        ? request.id()
                        : UUID.randomUUID().toString());
        return new Promotion(
                promotionId,
                request.restaurantId(),
                request.title(),
                request.description(),
                request.category(),
                request.price());
    }

    private PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.id(),
                promotion.restaurantId(),
                promotion.title(),
                promotion.description(),
                promotion.category(),
                promotion.price());
    }

    private Optional<Promotion> retrievePromotionById(String id) throws ApiException {
        try {
            return promotionRepository.getById(id);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch promotion {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private Optional<Restaurant> retrieveRestaurantById(String id) throws ApiException {
        try {
            return restaurantRepository.getById(id);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch restaurant {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private Optional<User> retrieveUserById(String id) throws ApiException {
        try {
            return userRepository.getById(id);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch user {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }

    private void validateRestaurant(String restaurantId) throws ApiException {
        var restaurantOpt = retrieveRestaurantById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            log.warn("Restaurant was not found. Id: {}", restaurantId);
            throw new ApiException(AppErrorCode.RESTAURANT_NOT_FOUND);
        }

        var restaurant = restaurantOpt.get();
        var userOpt = retrieveUserById(restaurant.userId());
        if (userOpt.isEmpty()) {
            log.warn("User associated with restaurant {} was not found.", restaurantId);
            throw new ApiException(AppErrorCode.USER_NOT_FOUND);
        }

        var user = userOpt.get();
        if (!User.UserType.RESTAURANT.equals(user.type())) {
            log.info("User {} associated with restaurant {} is not of type RESTAURANT.", user.id(), restaurantId);
            throw new ApiException(AppErrorCode.INVALID_USER_TYPE);
        }
    }
}