package com.example.localservices.service;

import com.example.localservices.dto.SearchCriteria;
import com.example.localservices.repository.ProviderSearchProjection;
import com.example.localservices.repository.ServiceProviderRepository;
import com.example.localservices.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SearchServiceImplTest {
    @Test
    void delegatesFilteringAndPaginationToDatabase() {
        ServiceProviderRepository repository = mock(ServiceProviderRepository.class);
        ProviderSearchProjection projection = mock(ProviderSearchProjection.class);
        when(projection.getId()).thenReturn(7L);
        when(projection.getBusinessName()).thenReturn("City Mechanic");
        when(projection.getReviewCount()).thenReturn(4);
        when(projection.getDistanceKm()).thenReturn(2.345);
        when(repository.search(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(projection)));
        SearchCriteria criteria = new SearchCriteria(" Mechanic ", "Auto-Services", new BigDecimal("41.998"),
                new BigDecimal("21.425"), new BigDecimal("5"), new BigDecimal("4"), "distance", 1, 10);

        var result = new SearchServiceImpl(repository).search(criteria);

        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(7L);
            assertThat(item.distanceKm()).isEqualTo(2.35);
        });
        verify(repository).search(eq("%mechanic%"), eq("auto-services"), eq(criteria.latitude()),
                eq(criteria.longitude()), eq(criteria.radius()), eq(criteria.minimumRating()), eq("distance"),
                argThat(page -> page.getPageNumber() == 1 && page.getPageSize() == 10));
    }
}
