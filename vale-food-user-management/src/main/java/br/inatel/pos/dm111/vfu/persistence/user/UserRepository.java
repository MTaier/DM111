package br.inatel.pos.dm111.vfu.persistence.user;

import br.inatel.pos.dm111.vfu.persistence.ValeFoodRepository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface UserRepository extends ValeFoodRepository<User> {

    Optional<User> getByEmail(String email) throws ExecutionException, InterruptedException;
}
