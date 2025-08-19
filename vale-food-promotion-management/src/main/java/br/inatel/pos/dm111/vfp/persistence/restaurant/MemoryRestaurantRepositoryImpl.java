package br.inatel.pos.dm111.vfp.persistence.restaurant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Profile("test")
@Component
public class MemoryRestaurantRepositoryImpl implements RestaurantRepository {

    private final Map<String, Restaurant> db = new HashMap<>();

    @Override
    public List<Restaurant> getAll() {
        return List.copyOf(db.values());
    }

    @Override
    public Optional<Restaurant> getById(String id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Optional<Restaurant> getByUserId(String userId) {
        return db.values().stream()
                .filter(r -> r.userId().equals(userId))
                .findAny();
    }

    @Override
    public Restaurant save(Restaurant entity) {
        db.put(entity.id(), entity);
        return entity;
    }

    @Override
    public void delete(String id) {
        db.remove(id);
    }
}