package com.example.localservices.controller;

import com.example.localservices.dto.AdminStatisticsResponse;
import com.example.localservices.dto.CategoryRequest;
import com.example.localservices.dto.CategoryResponse;
import com.example.localservices.service.AdminService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import(AdminControllerTest.MethodSecurityTestConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService service;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void statisticsReturnsCorrectData() throws Exception {
        AdminStatisticsResponse response = new AdminStatisticsResponse(
                10L, 9L, 4L, 1L, 3L,
                20L, 2L, 15L, 8L, 5L
        );

        when(service.statistics()).thenReturn(response);

        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.enabledUsers").value(9))
                .andExpect(jsonPath("$.totalProviders").value(4))
                .andExpect(jsonPath("$.pendingProviders").value(1))
                .andExpect(jsonPath("$.verifiedProviders").value(3))
                .andExpect(jsonPath("$.totalReservations").value(20))
                .andExpect(jsonPath("$.pendingReservations").value(2))
                .andExpect(jsonPath("$.completedReservations").value(15))
                .andExpect(jsonPath("$.totalReviews").value(8))
                .andExpect(jsonPath("$.totalCategories").value(5));

        verify(service).statistics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersReturnsOk() throws Exception {
        when(service.users(
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());

        verify(service).users(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProvidersReturnsOk() throws Exception {
        when(service.providers(
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/providers"))
                .andExpect(status().isOk());

        verify(service).providers(
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoriesReturnsOk() throws Exception {
        when(service.categories(any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk());

        verify(service).categories(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategoryReturnsCreatedCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(
                1L,
                "Home Cleaning",
                "home-cleaning",
                "Professional home cleaning services",
                true
        );

        when(service.createCategory(any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Home Cleaning",
                                    "slug": "home-cleaning",
                                    "description": "Professional home cleaning services",
                                    "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Home Cleaning"))
                .andExpect(jsonPath("$.slug").value("home-cleaning"))
                .andExpect(jsonPath("$.description")
                        .value("Professional home cleaning services"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategoryRejectsInvalidSlug() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Home Cleaning",
                                    "slug": "Home Cleaning",
                                    "description": "Professional home cleaning services",
                                    "active": true
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminCannotAccessStatistics() throws Exception {
        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }
}