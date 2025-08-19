package br.inatel.pos.dm111.vfp.persistence.user;

import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Profile("local")
@Component
public class FirebaseUserRepositoryImpl implements UserRepository {

    private static final String COLLECTION_NAME = "users";

    private final Firestore firestore;

    public FirebaseUserRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<User> getAll() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(User.class))
                .toList();
    }

    @Override
    public Optional<User> getById(String id) throws ExecutionException, InterruptedException {
        var user = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get()
                .toObject(User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> getByEmail(String email) throws ExecutionException, InterruptedException {
        var query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();
        if (query.isEmpty()) {
            return Optional.empty();
        }
        return query.getDocuments().stream()
                .map(doc -> doc.toObject(User.class))
                .findFirst();
    }

    @Override
    public User save(User entity) {
        firestore.collection(COLLECTION_NAME)
                .document(entity.id())
                .set(entity);
        return entity;
    }

    @Override
    public void delete(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .get();
    }
}