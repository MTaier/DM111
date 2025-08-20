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
import java.util.Locale;
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

    public AuthService(PrivateKey privateKey, UserRepository repository, PasswordEncryptor encryptor) {
        this.privateKey = privateKey;
        this.repository = repository;
        this.encryptor = encryptor;
    }

    public AuthResponse authenticate(AuthRequest req) throws ApiException {

        var email = req.email().toLowerCase(Locale.ROOT);
        var userOpt = retrieveUserByEmail(email);

        if (userOpt.isEmpty()) {
            log.info("Invalid credentials (user not found): {}", req.email());
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        var user = userOpt.get();
        var encryptedPwd = encryptor.encrypt(req.password());

        if (!encryptedPwd.equals(user.password())) {
            log.info("Invalid credentials (password mismatch): {}", req.email());
            throw new ApiException(AppErrorCode.INVALID_USER_CREDENTIALS);
        }

        var token = generateToken(user);
        return new AuthResponse(token);
    }

    private String generateToken(User user) {
        var now = Instant.now();

        return Jwts.builder()
                .issuer(tokenIssuer)
                .subject(user.email())
                .claim("role", user.type().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(privateKey)
                .compact();
    }

    private Optional<User> retrieveUserByEmail(String email) throws ApiException {
        try {
            return repository.getByEmail(email.toLowerCase(Locale.ROOT));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to read a user from DB by email.", e);
            throw new ApiException(AppErrorCode.INTERNAL_DATABASE_COMMUNICATION_ERROR);
        }
    }
}