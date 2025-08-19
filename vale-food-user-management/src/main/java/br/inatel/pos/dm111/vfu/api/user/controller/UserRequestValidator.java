package br.inatel.pos.dm111.vfu.api.user.controller;

import br.inatel.pos.dm111.vfu.api.user.UserRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

@Component
public class UserRequestValidator implements Validator {

    private static final List<String> VALID_USER_TYPES = List.of("REGULAR", "RESTAURANT");

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.empty", "Name is required!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.empty", "Email is required!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.empty", "Password is required!");

        var req = (UserRequest) target;
        if (!VALID_USER_TYPES.contains(req.type())) {
            errors.rejectValue("type", "user.type.invalid", "Invalid user type! Possible values: REGULAR, RESTAURANT");
        }
    }
}
