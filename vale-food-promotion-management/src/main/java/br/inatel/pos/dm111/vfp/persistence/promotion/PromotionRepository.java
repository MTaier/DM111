package br.inatel.pos.dm111.vfp.persistence.promotion;

import br.inatel.pos.dm111.vfp.persistence.ValeFoodRepository;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PromotionRepository extends ValeFoodRepository<Promotion> {
    List<Promotion> getByRestaurantId(String restaurantId) throws ExecutionException, InterruptedException;
}