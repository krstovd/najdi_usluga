package com.example.localservices.controller;

import com.example.localservices.dto.ProviderSearchResponse;
import com.example.localservices.dto.SearchCriteria;
import com.example.localservices.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/search")
public class SearchController {
    private final SearchService service;
    public SearchController(SearchService service) { this.service = service; }
    @GetMapping Page<ProviderSearchResponse> search(@Valid @ModelAttribute SearchCriteria criteria) {
        return service.search(criteria);
    }
}
