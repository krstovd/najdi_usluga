package com.example.localservices.service;

import com.example.localservices.dto.ProviderSearchResponse;
import com.example.localservices.dto.SearchCriteria;
import org.springframework.data.domain.Page;

public interface SearchService { Page<ProviderSearchResponse> search(SearchCriteria criteria); }
