package br.inatel.pos.dm111.vfa.persistence.user;

import br.inatel.pos.dm111.vfa.persistence.ValeFoodRepository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface UserRepository extends ValeFoodRepository<User> {

    Optional<User> getByEmail(String email) throws ExecutionException, InterruptedException;
}
