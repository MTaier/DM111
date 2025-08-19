package br.inatel.pos.dm111.vfr.api.user.service;

import br.inatel.pos.dm111.vfr.api.user.UserRequest;
import br.inatel.pos.dm111.vfr.persistence.user.User;
import br.inatel.pos.dm111.vfr.persistence.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }


    public UserRequest createUser(UserRequest request) {
        var user = buildUser(request);
        repository.save(user);
        log.info("User was successfully created. Id: {}", user.id());

        return request;
    }

    private User buildUser(UserRequest request) {
        return new User(request.id(), request.name(), request.email(), null, User.UserType.valueOf(request.type()));
    }
}
