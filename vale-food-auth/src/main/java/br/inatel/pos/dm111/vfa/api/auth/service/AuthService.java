package br.inatel.pos.dm111.vfa.api.auth.service;

import br.inatel.pos.dm111.vfa.api.PasswordEncryptor;
import br.inatel.pos.dm111.vfa.api.auth.AuthRequest;
import br.inatel.pos.dm111.vfa.api.auth.AuthResponse;
import br.inatel.pos.dm111.vfa.api.core.ApiException;
import br.inatel.pos.dm111.vfa.api.core.AppErrorCode;
import br.inatel.pos.dm111.vfa.persistence.user.User;
import br.inatel.pos.dm111.vfa.persistence.user.UserRepository;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Value("${vale-food.jwt.custom.issuer}")
    private String tokenIssuer;

    private final PrivateKey privateKey;
    private final UserRepository repository;
    private final PasswordEncryptor encryptor;

    public AuthService(PrivateKey privateKey,
            UserRepository repository,
            PasswordEncryptor encryptor) {
        this.privateKey = privateKey;
        this.repository = repository;
        this.encryptor = encryptor;
    }

    public AuthResponse authenticate(AuthRequest request) throws ApiException {
        var userOpt = retrieveUserByEmail(request.email());

        if (userOpt.isPresent()) {
            var user = userOpt.get();
            var encryptedPwd = encryptor.encrypt(request.password());

            if (encryptedPwd.equals(user.password())) {
                var token = generateToken(user);
                return new AuthResponse(token);
            }
        } else {
            log.info("Invalid credentials provided.");
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }
        return null;
    }

    private String generateToken(User user) {
        var now = Instant.now();

        return Jwts.builder()
                .issuer(tokenIssuer)
                .subject(user.email())
                .claim("role", user.type())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(600)))
                .signWith(privateKey)
                .compact();
    }

    private Optional<User> retrieveUserByEmail(String email) throws ApiException {
        try {
            return repository.getByEmail(email);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read an users from DB by email.", e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }
}
