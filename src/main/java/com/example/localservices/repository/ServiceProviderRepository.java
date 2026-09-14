package com.example.localservices.repository;

import com.example.localservices.entity.ServiceProvider;
import com.example.localservices.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Optional<ServiceProvider> findByOwnerId(Long ownerId);
    Optional<ServiceProvider> findByIdAndVerificationStatus(Long id, VerificationStatus status);
    Page<ServiceProvider> findAllByVerificationStatus(VerificationStatus status, Pageable pageable);
    long countByVerificationStatus(VerificationStatus status);
    @Query("select p from ServiceProvider p where (:query is null or lower(p.businessName) like :query or lower(p.city) like :query or lower(p.owner.email) like :query) and (:status is null or p.verificationStatus = :status)")
    Page<ServiceProvider> adminSearch(@Param("query") String query, @Param("status") VerificationStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ServiceProvider p where p.id = :id")
    Optional<ServiceProvider> findByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            SELECT p.id AS id, p.business_name AS businessName, c.id AS categoryId, c.name AS categoryName,
                   p.description AS description, p.address AS address, p.city AS city,
                   p.latitude AS latitude, p.longitude AS longitude, p.average_rating AS averageRating,
                   p.review_count AS reviewCount,
                   CASE WHEN :latitude IS NULL THEN NULL ELSE
                     ST_Distance_Sphere(POINT(p.longitude, p.latitude), POINT(:longitude, :latitude)) / 1000.0
                   END AS distanceKm
            FROM service_providers p
            JOIN categories c ON c.id = p.category_id
            WHERE p.verification_status = 'VERIFIED'
              AND c.active = TRUE
              AND (:queryText IS NULL OR LOWER(p.business_name) LIKE :queryText
                   OR LOWER(p.description) LIKE :queryText OR LOWER(c.name) LIKE :queryText
                   OR LOWER(p.city) LIKE :queryText OR LOWER(p.address) LIKE :queryText)
              AND (:category IS NULL OR LOWER(c.slug) = :category OR LOWER(c.name) = :category)
              AND (:minimumRating IS NULL OR p.average_rating >= :minimumRating)
              AND (:latitude IS NULL OR ST_Distance_Sphere(
                    POINT(p.longitude, p.latitude), POINT(:longitude, :latitude)) <= :radiusKm * 1000.0)
            ORDER BY
              CASE WHEN :sortMode = 'distance' THEN ST_Distance_Sphere(
                    POINT(p.longitude, p.latitude), POINT(:longitude, :latitude)) END ASC,
              CASE WHEN :sortMode = 'rating' THEN p.average_rating END DESC,
              CASE WHEN :sortMode = 'reviews' THEN p.review_count END DESC,
              CASE WHEN :sortMode = 'name' THEN p.business_name END ASC,
              p.id ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM service_providers p
            JOIN categories c ON c.id = p.category_id
            WHERE p.verification_status = 'VERIFIED'
              AND c.active = TRUE
              AND (:queryText IS NULL OR LOWER(p.business_name) LIKE :queryText
                   OR LOWER(p.description) LIKE :queryText OR LOWER(c.name) LIKE :queryText
                   OR LOWER(p.city) LIKE :queryText OR LOWER(p.address) LIKE :queryText)
              AND (:category IS NULL OR LOWER(c.slug) = :category OR LOWER(c.name) = :category)
              AND (:minimumRating IS NULL OR p.average_rating >= :minimumRating)
              AND (:latitude IS NULL OR ST_Distance_Sphere(
                    POINT(p.longitude, p.latitude), POINT(:longitude, :latitude)) <= :radiusKm * 1000.0)
            """, nativeQuery = true)
    Page<ProviderSearchProjection> search(
            @Param("queryText") String queryText,
            @Param("category") String category,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") BigDecimal radiusKm,
            @Param("minimumRating") BigDecimal minimumRating,
            @Param("sortMode") String sortMode,
            Pageable pageable);
}
