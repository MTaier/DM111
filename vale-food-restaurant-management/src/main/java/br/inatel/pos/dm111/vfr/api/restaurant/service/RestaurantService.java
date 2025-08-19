package br.inatel.pos.dm111.vfr.api.restaurant.service;

import br.inatel.pos.dm111.vfr.api.core.ApiException;
import br.inatel.pos.dm111.vfr.api.core.AppErrorCode;
import br.inatel.pos.dm111.vfr.api.restaurant.ProductRequest;
import br.inatel.pos.dm111.vfr.api.restaurant.ProductResponse;
import br.inatel.pos.dm111.vfr.api.restaurant.RestaurantRequest;
import br.inatel.pos.dm111.vfr.api.restaurant.RestaurantResponse;
import br.inatel.pos.dm111.vfr.persistence.restaurant.Product;
import br.inatel.pos.dm111.vfr.persistence.restaurant.Restaurant;
import br.inatel.pos.dm111.vfr.persistence.restaurant.RestaurantRepository;
import br.inatel.pos.dm111.vfr.persistence.user.User;
import br.inatel.pos.dm111.vfr.persistence.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository repository, UserRepository userRepository) {
        this.restaurantRepository = repository;
        this.userRepository = userRepository;
    }

    public List<RestaurantResponse> searchRestaurants() throws ApiException {
        return retrieveRestaurants().stream()
                .map(this::buildRestaurantResponse)
                .toList();
    }

    public RestaurantResponse searchRestaurant(String id) throws ApiException {
        return retrieveRestaurantById(id)
                .map(this::buildRestaurantResponse)
                .orElseThrow(() -> {
                    log.warn("Restaurant was not found. Id: {}", id);
                    return new ApiException(AppErrorCode.RESTAURANT_NOT_FOUND);
                });
    }

    public RestaurantResponse createRestaurant(RestaurantRequest request) throws ApiException {
        // validate user exist and its type is RESTAURANT
        validateRestaurant(request);

        var restaurant = buildRestaurant(request);
        restaurantRepository.save(restaurant);
        log.info("Restaurant was successfully created. Id: {}", restaurant.id());

        return buildRestaurantResponse(restaurant);
    }

    public RestaurantResponse updateRestaurant(RestaurantRequest request, String id) throws ApiException {
        // check restaurant by id exist
        var restaurantOpt = retrieveRestaurantById(id);

        if (restaurantOpt.isEmpty()) {
            log.warn("Restaurant was not found. Id: {}", id);
            throw new ApiException(AppErrorCode.RESTAURANT_NOT_FOUND);
        } else {
            var restaurant = restaurantOpt.get();

            validateRestaurant(request);

            var updatedRestaurant = buildRestaurant(request, restaurant.id());
            restaurantRepository.save(updatedRestaurant);
            log.info("Restaurant was successfully updated. Id: {}", restaurant.id());

            return buildRestaurantResponse(updatedRestaurant);
        }
    }

    public void removeRestaurant(String id) throws ApiException {
        var restaurantOpt = retrieveRestaurantById(id);
        if (restaurantOpt.isPresent()) {
            try {
                restaurantRepository.delete(id);
            } catch (ExecutionException | InterruptedException e) {
                log.error("Failed to delete a restaurant from DB by id {}.", id, e);
                throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
            }
        } else {
            log.info("The provided restaurant id was not found. id: {}", id);
        }

    }

    private void validateRestaurant(RestaurantRequest request) throws ApiException {
        var userOpt = retrieveUserById(request.userId());

        if (userOpt.isEmpty()) {
            log.warn("User was not found. Id: {}", request.userId());
            throw new ApiException(AppErrorCode.USER_NOT_FOUND);
        } else {
            var user = userOpt.get();
            if (!User.UserType.RESTAURANT.equals(user.type())) {
                log.info("User provided is not valid for this operation. UserId: {}", request.userId());
                throw new ApiException(AppErrorCode.INVALID_USER_TYPE);
            }
        }
    }

    private Restaurant buildRestaurant(RestaurantRequest request) {
        var id = UUID.randomUUID().toString();
        return buildRestaurant(request, id);
    }

    private Restaurant buildRestaurant(RestaurantRequest request, String id) {
        var products = request.products().stream()
                .map(this::buildProduct)
                .toList();

        return new Restaurant(id,
                request.name(),
                request.address(),
                request.userId(),
                request.categories(),
                products);
    }

    private Product buildProduct(ProductRequest request) {
        var id = UUID.randomUUID().toString();

        return new Product(id,
                request.name(),
                request.description(),
                request.category(),
                request.price());
    }

    private RestaurantResponse buildRestaurantResponse(Restaurant restaurant) {
        var products = restaurant.products().stream()
                .map(this::buildProductResponse)
                .toList();

        return new RestaurantResponse(restaurant.id(),
                restaurant.name(),
                restaurant.address(),
                restaurant.userId(),
                restaurant.categories(),
                products);
    }

    private ProductResponse buildProductResponse(Product product) {
        return new ProductResponse(product.id(),
                product.name(),
                product.description(),
                product.category(),
                product.price());
    }

    private List<Restaurant> retrieveRestaurants() throws ApiException {
        try {
            return restaurantRepository.getAll();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read all restaurants from DB.", e);
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

    private Optional<User> retrieveUserById(String id) throws ApiException {
        try {
            return userRepository.getById(id);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read an user from DB by id {}.", id, e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }
}
