package br.inatel.pos.dm111.vfp.persistence;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface ValeFoodRepository<T> {

    List<T> getAll() throws ExecutionException, InterruptedException;

    Optional<T> getById(String id) throws ExecutionException, InterruptedException;

    T save(T entity);

    void delete(String id) throws ExecutionException, InterruptedException;
}