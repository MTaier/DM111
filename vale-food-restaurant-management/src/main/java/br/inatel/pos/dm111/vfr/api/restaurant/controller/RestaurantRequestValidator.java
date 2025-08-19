package br.inatel.pos.dm111.vfr.api.restaurant.controller;

import br.inatel.pos.dm111.vfr.api.restaurant.RestaurantRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class RestaurantRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RestaurantRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.empty", "Name is required!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "address.empty", "Address is required!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userId", "userId.empty", "User Id is required!");
        ValidationUtils.rejectIfEmpty(errors, "categories", "categories.empty", "Categories are required!");
    }
}
