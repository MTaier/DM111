package br.inatel.pos.dm111.vfr.persistence.restaurant;

import br.inatel.pos.dm111.vfr.persistence.ValeFoodRepository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface RestaurantRepository extends ValeFoodRepository<Restaurant> {

    Optional<Restaurant> getByUserId(String userId) throws ExecutionException, InterruptedException;
}
