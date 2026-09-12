package com.example.localservices.repository;

import com.example.localservices.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlugAndActiveTrue(String slug);
    Page<Category> findAllByActiveTrue(Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
}
