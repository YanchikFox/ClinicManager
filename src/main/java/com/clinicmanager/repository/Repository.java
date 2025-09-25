package com.clinicmanager.repository;

import java.util.List;

public interface Repository<T> {
    int save(T entity);

    void delete(T entity);

    void update(T entity);

    T findById(int id);

    List<T> findAll();

    T findByEmail(String email); // may return null if not applicable
}
