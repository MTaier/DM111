package br.inatel.pos.dm111.vfa.api.auth.controller;

import br.inatel.pos.dm111.vfa.api.auth.AuthRequest;
import br.inatel.pos.dm111.vfa.api.auth.AuthResponse;
import br.inatel.pos.dm111.vfa.api.auth.service.AuthService;
import br.inatel.pos.dm111.vfa.api.core.ApiException;
import br.inatel.pos.dm111.vfa.api.core.AppError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/valefood/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthRequestValidator validator;
    private final AuthService service;

    public AuthController(AuthRequestValidator validator, AuthService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest request,
                                             BindingResult bindingResult) throws ApiException {
        log.info("Trying to authenticate...");

        validateRequest(request, bindingResult);
        var response = service.authenticate(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);

    }

    private void validateRequest(AuthRequest request, BindingResult bindingResult) throws ApiException {
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
