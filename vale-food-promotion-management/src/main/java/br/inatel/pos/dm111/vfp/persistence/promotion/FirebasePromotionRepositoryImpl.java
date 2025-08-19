package br.inatel.pos.dm111.vfp.persistence.promotion;

import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Profile("local")
@Component
public class FirebasePromotionRepositoryImpl implements PromotionRepository {

    private static final String COLLECTION_NAME = "promotions";

    private final Firestore firestore;

    public FirebasePromotionRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<Promotion> getAll() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Promotion.class))
                .toList();
    }

    @Override
    public Optional<Promotion> getById(String id) throws ExecutionException, InterruptedException {
        var promo = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get()
                .toObject(Promotion.class);
        return Optional.ofNullable(promo);
    }

    @Override
    public Promotion save(Promotion entity) {
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

    @Override
    public List<Promotion> getByRestaurantId(String restaurantId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .whereEqualTo("restaurantId", restaurantId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Promotion.class))
                .toList();
    }
}