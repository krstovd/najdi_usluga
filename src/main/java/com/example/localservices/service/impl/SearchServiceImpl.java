package com.example.localservices.service.impl;

import com.example.localservices.dto.ProviderSearchResponse;
import com.example.localservices.dto.SearchCriteria;
import com.example.localservices.repository.ServiceProviderRepository;
import com.example.localservices.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class SearchServiceImpl implements SearchService {
    private final ServiceProviderRepository repository;
    public SearchServiceImpl(ServiceProviderRepository repository) { this.repository = repository; }

    @Override @Transactional(readOnly = true)
    public Page<ProviderSearchResponse> search(SearchCriteria criteria) {
        String query = normalize(criteria.query());
        if (query != null) query = "%" + escapeLike(query) + "%";
        String category = normalize(criteria.category());
        return repository.search(query, category, criteria.latitude(), criteria.longitude(), criteria.radius(),
                criteria.minimumRating(), criteria.sort(), PageRequest.of(criteria.page(), criteria.size()))
                .map(ProviderSearchResponse::from);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }
    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
