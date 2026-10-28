package com.example.localservices.service;

import com.example.localservices.entity.Category;
import com.example.localservices.repository.CategoryRepository;
import com.example.localservices.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceImplTest {
    private final CategoryRepository repository = mock(CategoryRepository.class);
    private final CategoryServiceImpl service = new CategoryServiceImpl(repository);

    @Test
    void listActiveMapsRepositoryPageToCategoryResponses() {
        var pageable = PageRequest.of(1, 2);
        var categories = List.of(
                new Category("Plumbing", "plumbing"),
                new Category("Electrical", "electrical"));
        when(repository.findAllByActiveTrue(pageable)).thenReturn(new PageImpl<>(categories, pageable, 5));

        var result = service.listActive(pageable);

        assertThat(result.getContent())
                .extracting("name", "slug", "active")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Plumbing", "plumbing", true),
                        org.assertj.core.groups.Tuple.tuple("Electrical", "electrical", true));
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getNumber()).isEqualTo(1);
        verify(repository).findAllByActiveTrue(pageable);
    }
}
