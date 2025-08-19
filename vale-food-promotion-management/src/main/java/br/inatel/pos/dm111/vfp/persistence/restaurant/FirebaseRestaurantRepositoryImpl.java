package br.inatel.pos.dm111.vfp.persistence.restaurant;

import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Profile("local")
@Component
public class FirebaseRestaurantRepositoryImpl implements RestaurantRepository {

    private static final String COLLECTION_NAME = "restaurants";

    private final Firestore firestore;

    public FirebaseRestaurantRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<Restaurant> getAll() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Restaurant.class))
                .toList();
    }

    @Override
    public Optional<Restaurant> getById(String id) throws ExecutionException, InterruptedException {
        var restaurant = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get()
                .toObject(Restaurant.class);
        return Optional.ofNullable(restaurant);
    }

    @Override
    public Optional<Restaurant> getByUserId(String userId) throws ExecutionException, InterruptedException {
        var query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .get();
        if (query.isEmpty()) {
            return Optional.empty();
        }
        return query.getDocuments().stream()
                .map(doc -> doc.toObject(Restaurant.class))
                .findFirst();
    }

    @Override
    public Restaurant save(Restaurant entity) {
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