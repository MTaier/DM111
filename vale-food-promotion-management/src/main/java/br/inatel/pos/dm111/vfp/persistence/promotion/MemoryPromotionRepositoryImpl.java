package br.inatel.pos.dm111.vfp.persistence.promotion;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Profile("test")
@Component
public class MemoryPromotionRepositoryImpl implements PromotionRepository {

    private final Map<String, Promotion> db = new HashMap<>();

    @Override
    public List<Promotion> getAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    public Optional<Promotion> getById(String id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Promotion save(Promotion entity) {
        db.put(entity.id(), entity);
        return entity;
    }

    @Override
    public void delete(String id) {
        db.remove(id);
    }

    @Override
    public List<Promotion> getByRestaurantId(String restaurantId) {
        return db.values().stream()
                .filter(p -> p.restaurantId().equals(restaurantId))
                .toList();
    }
}