package com.example.commerce.specifications;

import com.example.commerce.entities.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class UserSpecifications {

    public static Specification<UserEntity> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            
            String likePattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
            );
        };
    }
}
