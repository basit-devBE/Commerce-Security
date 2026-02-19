package com.example.commerce.specifications;

import com.example.commerce.entities.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {
    
    public static Specification<CategoryEntity> searchByKeyword(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
