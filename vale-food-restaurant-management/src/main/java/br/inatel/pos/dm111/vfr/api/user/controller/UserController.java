package br.inatel.pos.dm111.vfr.api.user.controller;

import br.inatel.pos.dm111.vfr.api.user.UserRequest;
import br.inatel.pos.dm111.vfr.api.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/valefood/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserRequest> postUser(@RequestBody UserRequest request) {
        log.debug("Received request to create a new user into the cache...");

        var response = service.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
