package com.example.localservices.service;

import com.example.localservices.dto.ProviderRequest;
import com.example.localservices.entity.*;
import com.example.localservices.repository.*;
import com.example.localservices.service.impl.ProviderServiceImpl;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProviderServiceImplTest {
    private final ServiceProviderRepository providers = mock(ServiceProviderRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final AvailabilityRepository availability = mock(AvailabilityRepository.class);
    private final ProviderServiceImpl service = new ProviderServiceImpl(providers, users, categories, availability);

    @Test
    void createStartsPendingAndPromotesOwnerRole() {
        User owner = new User("Ana", "Test", "ana@example.com", "hash", UserRole.USER);
        Category category = new Category("Repair", "repair");
        when(users.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(owner));
        when(providers.findByOwnerId(null)).thenReturn(Optional.empty());
        when(categories.findById(1L)).thenReturn(Optional.of(category));
        when(providers.save(any(ServiceProvider.class))).thenAnswer(i -> i.getArgument(0));

        var response = service.create("ana@example.com", request());

        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(owner.getRole()).isEqualTo(UserRole.PROVIDER);
        verify(providers).save(any(ServiceProvider.class));
    }

    private ProviderRequest request() {
        return new ProviderRequest(1L, "Fix It", "Repairs", "Main Street 1", "Skopje",
                new BigDecimal("41.9981000"), new BigDecimal("21.4254000"), "+38970111222",
                "shop@example.com", "https://example.com");
    }
}
