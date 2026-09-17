package com.example.localservices.repository;

import com.example.localservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.localservices.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByEnabledTrue();
    long countByRole(UserRole role);
    @Query("select u from User u where (:query is null or lower(u.firstName) like :query or lower(u.lastName) like :query or lower(u.email) like :query) and (:role is null or u.role = :role) and (:enabled is null or u.enabled = :enabled)")
    Page<User> adminSearch(@Param("query") String query, @Param("role") UserRole role, @Param("enabled") Boolean enabled, Pageable pageable);
}
