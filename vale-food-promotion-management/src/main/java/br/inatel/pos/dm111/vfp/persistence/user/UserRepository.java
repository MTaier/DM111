package br.inatel.pos.dm111.vfp.persistence.user;

import br.inatel.pos.dm111.vfp.persistence.ValeFoodRepository;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Repository contract for {@link User} entities used by the promotion service.
 */
public interface UserRepository extends ValeFoodRepository<User> {

    Optional<User> getByEmail(String email) throws ExecutionException, InterruptedException;
}