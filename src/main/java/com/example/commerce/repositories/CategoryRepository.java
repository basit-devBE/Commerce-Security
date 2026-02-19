package com.example.commerce.repositories;

import com.example.commerce.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>, JpaSpecificationExecutor<CategoryEntity> {
    boolean existsByNameIgnoreCase(String name);
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
