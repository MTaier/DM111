package br.inatel.pos.dm111.vfr.api.restaurant.controller;

import br.inatel.pos.dm111.vfr.api.core.ApiException;
import br.inatel.pos.dm111.vfr.api.core.AppError;
import br.inatel.pos.dm111.vfr.api.restaurant.RestaurantRequest;
import br.inatel.pos.dm111.vfr.api.restaurant.RestaurantResponse;
import br.inatel.pos.dm111.vfr.api.restaurant.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/valefood/restaurants")
public class RestaurantController {

    private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);

    private final RestaurantRequestValidator validator;
    private final RestaurantService service;

    public RestaurantController(RestaurantRequestValidator validator, RestaurantService service) {
        this.validator = validator;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() throws ApiException {
        log.debug("Received request to list all restaurants");

        var response = service.searchRestaurants();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping(value = "/{restaurantId}")
    public ResponseEntity<RestaurantResponse> getUserById(@PathVariable("restaurantId") String id)
            throws ApiException {
        log.debug("Received request to list an restaurant by id: {}", id);

        var response = service.searchRestaurant(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> postRestaurant(@RequestBody RestaurantRequest request,
                                                 BindingResult bindingResult)
            throws ApiException {
        log.debug("Received request to create a new user...");

        validateRequest(request, bindingResult);
        var response = service.createRestaurant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping(value = "/{restaurantId}")
    public ResponseEntity<RestaurantResponse> putUser(@RequestBody RestaurantRequest request,
                                                @PathVariable("restaurantId") String restaurantId,
                                                BindingResult bindingResult)
            throws ApiException {
        log.debug("Received request to update an user...");

        validateRequest(request, bindingResult);

        var response = service.updateRestaurant(request, restaurantId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping(value = "/{restaurantId}")
    public ResponseEntity<List<RestaurantResponse>> deleteRestaurant(@PathVariable("restaurantId") String id)
            throws ApiException {
        log.debug("Received request to delete an restaurant: id {}", id);

        service.removeRestaurant(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    private void validateRequest(RestaurantRequest request, BindingResult bindingResult)
            throws ApiException {
        ValidationUtils.invokeValidator(validator, request, bindingResult);

        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(fe -> new AppError(fe.getCode(), fe.getDefaultMessage()))
                    .toList();
            throw new ApiException(HttpStatus.BAD_REQUEST, errors);
        }
    }
}
