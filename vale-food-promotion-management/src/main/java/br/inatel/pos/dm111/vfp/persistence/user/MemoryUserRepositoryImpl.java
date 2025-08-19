package br.inatel.pos.dm111.vfp.persistence.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Profile("test")
@Component
public class MemoryUserRepositoryImpl implements UserRepository {

    private final Map<String, User> db = new HashMap<>();

    @Override
    public List<User> getAll() {
        return List.copyOf(db.values());
    }

    @Override
    public Optional<User> getById(String id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return db.values().stream()
                .filter(u -> u.email().equals(email))
                .findAny();
    }

    @Override
    public User save(User entity) {
        db.put(entity.id(), entity);
        return entity;
    }

    @Override
    public void delete(String id) {
        db.remove(id);
    }
}