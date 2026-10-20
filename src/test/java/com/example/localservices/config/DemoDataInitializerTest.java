package com.example.localservices.config;

import com.example.localservices.entity.*;
import com.example.localservices.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemoDataInitializerTest {
    private final UserRepository users = mock(UserRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final ServiceProviderRepository providers = mock(ServiceProviderRepository.class);
    private final AvailabilityRepository availability = mock(AvailabilityRepository.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    private Category category() {
        var category = new Category("Demo", "demo");
        ReflectionTestUtils.setField(category, "id", 1L);
        return category;
    }

    private DemoDataInitializer initializer(String password) {
        return new DemoDataInitializer(users, categories, providers, availability, encoder, mapper,
                Validation.buildDefaultValidatorFactory().getValidator(), password);
    }

    @Test
    void createsCompleteValidatedCatalogAndDoesNotWriteAgainOnRestart() throws Exception {
        Set<String> emails = new HashSet<>();
        List<User> owners = new ArrayList<>();
        List<ServiceProvider> saved = new ArrayList<>();
        List<Availability> hours = new ArrayList<>();
        when(categories.findBySlugAndActiveTrue(anyString())).thenAnswer(call -> {
            Category category = new Category(call.getArgument(0), call.getArgument(0));
            ReflectionTestUtils.setField(category, "id", 1L);
            return Optional.of(category);
        });
        when(users.existsByEmailIgnoreCase(anyString())).thenAnswer(call -> emails.contains(call.getArgument(0)));
        when(users.save(any())).thenAnswer(call -> {
            User user = call.getArgument(0);
            ReflectionTestUtils.setField(user, "id", (long) owners.size() + 1);
            emails.add(user.getEmail());
            owners.add(user);
            return user;
        });
        when(providers.save(any())).thenAnswer(call -> {
            ServiceProvider provider = call.getArgument(0);
            saved.add(provider);
            return provider;
        });
        when(availability.save(any())).thenAnswer(call -> {
            Availability value = call.getArgument(0);
            hours.add(value);
            return value;
        });
        var runner = initializer("DemoProvider2026!");
        runner.run(new DefaultApplicationArguments());
        assertThat(saved).hasSize(36);
        assertThat(owners).hasSize(36).allSatisfy(owner -> {
            assertThat(owner.getRole()).isEqualTo(UserRole.PROVIDER);
            assertThat(owner.isEnabled()).isTrue();
            assertThat(encoder.matches("DemoProvider2026!", owner.getPasswordHash())).isTrue();
            assertThat(owner.getEmail()).endsWith("@example.com");
        });
        assertThat(saved).allSatisfy(provider -> {
            assertThat(provider.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
            assertThat(provider.getAverageRating()).isZero();
            assertThat(provider.getReviewCount()).isZero();
            assertThat(provider.getDescription()).contains("измислен демонстрациски").hasSizeGreaterThan(300);
        });
        assertThat(saved.stream().map(ServiceProvider::getCity).distinct()).hasSize(6);
        saved.stream().collect(java.util.stream.Collectors.groupingBy(ServiceProvider::getCity))
                .values().forEach(city -> assertThat(city).hasSize(6));
        saved.stream().collect(java.util.stream.Collectors.groupingBy(p -> p.getCategory().getSlug()))
                .values().forEach(category -> assertThat(category).hasSize(3));
        assertThat(hours).hasSize(252).allSatisfy(value -> {
            if (value.isClosed()) {
                assertThat(value.getStartTime()).isNull();
                assertThat(value.getEndTime()).isNull();
            } else {
                assertThat(value.getStartTime()).isBefore(value.getEndTime());
            }
        });
        // Simulate a user editing the profile. A restart must retain the edit and password.
        saved.getFirst().setBusinessName("Корисничка измена");
        String hash = owners.getFirst().getPasswordHash();
        runner.run(new DefaultApplicationArguments());
        assertThat(saved).hasSize(36);
        assertThat(hours).hasSize(252);
        assertThat(saved.getFirst().getBusinessName()).isEqualTo("Корисничка измена");
        assertThat(owners.getFirst().getPasswordHash()).isEqualTo(hash);
    }

    @Test
    void skipsAnyExistingAccountWithoutChangingItEvenIfItHasNoProvider() throws Exception {
        when(categories.findBySlugAndActiveTrue(anyString())).thenReturn(Optional.of(category()));
        when(users.existsByEmailIgnoreCase(anyString())).thenReturn(true);
        initializer("").run(new DefaultApplicationArguments());
        verify(users, never()).save(any());
        verifyNoInteractions(providers, availability);
    }

    @Test
    void missingCategoryFailsBeforeWriting() {
        when(categories.findBySlugAndActiveTrue(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> initializer("DemoProvider2026!").run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("active category");
        verify(users, never()).save(any());
        verifyNoInteractions(providers, availability);
    }

    @Test
    void missingPasswordFailsBeforeWriting() {
        when(categories.findBySlugAndActiveTrue(anyString())).thenReturn(Optional.of(category()));
        assertThatThrownBy(() -> initializer("").run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("DEMO_PROVIDER_PASSWORD");
        verify(users, never()).save(any());
    }

    @Test
    void fixtureSchedulesCoverEveryCategoryAndDay() throws Exception {
        try (var input = new ClassPathResource("demo/providers.json").getInputStream()) {
            List<DemoDataInitializer.DemoProvider> entries = mapper.readValue(input, new TypeReference<>() { });
            assertThat(entries).hasSize(36);
            for (var entry : entries) {
                for (DayOfWeek day : DayOfWeek.values()) {
                    var hours = DemoDataInitializer.hoursFor(entry.category(), day);
                    if (!hours.closed()) assertThat(hours.start()).isBefore(hours.end());
                }
            }
        }
    }

    @Test
    void productionAndDefaultStartupNeverRegisterInitializer() {
        var runner = new ApplicationContextRunner().withUserConfiguration(DemoDataInitializer.class);
        runner.withPropertyValues("spring.profiles.active=local")
                .run(context -> assertThat(context).doesNotHaveBean(DemoDataInitializer.class));
        runner.withPropertyValues("spring.profiles.active=prod,local", "app.demo-data.enabled=true")
                .run(context -> assertThat(context).doesNotHaveBean(DemoDataInitializer.class));
    }
}
