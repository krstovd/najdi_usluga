package com.example.localservices.config;

import com.example.localservices.dto.ProviderRequest;
import com.example.localservices.dto.RegisterRequest;
import com.example.localservices.entity.*;
import com.example.localservices.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Append-only fictional fixtures. Never repairs or resets an existing account. */
@Component
@Profile("local & !prod")
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private final UserRepository users;
    private final CategoryRepository categories;
    private final ServiceProviderRepository providers;
    private final AvailabilityRepository availability;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final String password;

    public DemoDataInitializer(UserRepository users, CategoryRepository categories,
            ServiceProviderRepository providers, AvailabilityRepository availability,
            PasswordEncoder encoder, ObjectMapper mapper, Validator validator,
            @Value("${app.demo-data.password:}") String password) {
        this.users = users;
        this.categories = categories;
        this.providers = providers;
        this.availability = availability;
        this.encoder = encoder;
        this.mapper = mapper;
        this.validator = validator;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<DemoProvider> catalog;
        try (var input = new ClassPathResource("demo/providers.json").getInputStream()) {
            catalog = mapper.readValue(input, new TypeReference<List<DemoProvider>>() { });
        }
        Map<String, Category> activeCategories = new HashMap<>();
        Set<String> keys = new HashSet<>();
        // Validate the complete catalog before writing. Any failure rolls back the batch.
        for (DemoProvider entry : catalog) {
            if (entry.key() == null || !entry.key().matches("[a-z0-9-]+") || !keys.add(entry.key())) {
                throw new IllegalArgumentException("Demo provider keys must be unique lowercase slugs");
            }
            Category category = activeCategories.computeIfAbsent(entry.category(), slug ->
                    categories.findBySlugAndActiveTrue(slug).orElseThrow(() ->
                            new IllegalStateException("Demo data requires active category: " + slug)));
            validate(entry.request(category.getId()));
        }
        int created = 0;
        int skipped = 0;
        for (DemoProvider entry : catalog) {
            // Stable reserved email identifies a fixture across restarts. Skip collisions
            // completely: never adopt an existing account or overwrite profile edits.
            if (users.existsByEmailIgnoreCase(entry.email())) {
                skipped++;
                continue;
            }
            if (password.isBlank() || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("Set DEMO_PROVIDER_PASSWORD to 8-72 characters (at most 72 UTF-8 bytes)");
            }
            validate(new RegisterRequest(entry.firstName(), entry.lastName(), entry.email(), password, entry.phone()));
            User owner = new User(entry.firstName(), entry.lastName(), entry.email(),
                    encoder.encode(password), UserRole.PROVIDER);
            owner.setPhone(entry.phone());
            owner = users.save(owner);
            ServiceProvider provider = new ServiceProvider(owner,
                    activeCategories.get(entry.category()), entry.businessName());
            provider.setDescription(entry.description());
            provider.setAddress(entry.address());
            provider.setCity(entry.city());
            provider.setLatitude(entry.latitude());
            provider.setLongitude(entry.longitude());
            provider.setPhone(entry.phone());
            provider.setEmail(entry.email());
            provider.setWebsite(entry.website());
            provider.setVerificationStatus(VerificationStatus.VERIFIED);
            provider = providers.save(provider);
            for (DayOfWeek day : DayOfWeek.values()) {
                Availability hours = new Availability(provider, day);
                Hours schedule = hoursFor(entry.category(), day);
                hours.setClosed(schedule.closed());
                hours.setStartTime(schedule.start());
                hours.setEndTime(schedule.end());
                availability.save(hours);
            }
            created++;
        }
        log.info("Fictional demo catalog: {} providers created, {} existing accounts skipped. No existing data changed.",
                created, skipped);
    }

    private <T> void validate(T value) {
        var violations = validator.validate(value);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Invalid demo fixture; check catalog and demo password", violations);
        }
    }

    static Hours hoursFor(String category, DayOfWeek day) {
        boolean sunday = day == DayOfWeek.SUNDAY;
        boolean weekend = sunday || day == DayOfWeek.SATURDAY;
        return switch (category) {
            case "restaurants" -> new Hours(LocalTime.of(9, 0), LocalTime.of(21, 0), false);
            case "fitness" -> sunday ? Hours.closedDay()
                    : new Hours(LocalTime.of(weekend ? 9 : 7, 0), LocalTime.of(weekend ? 15 : 21, 0), false);
            case "education" -> weekend ? Hours.closedDay()
                    : new Hours(LocalTime.of(14, 0), LocalTime.of(20, 0), false);
            case "medical", "legal" -> weekend ? Hours.closedDay()
                    : new Hours(LocalTime.of(9, 0), LocalTime.of(17, 0), false);
            case "hair-salons", "beauty" -> sunday ? Hours.closedDay()
                    : new Hours(LocalTime.of(10, 0), LocalTime.of(weekend ? 16 : 19, 0), false);
            default -> sunday ? Hours.closedDay()
                    : new Hours(LocalTime.of(weekend ? 9 : 8, 0), LocalTime.of(weekend ? 14 : 17, 0), false);
        };
    }

    record Hours(LocalTime start, LocalTime end, boolean closed) {
        static Hours closedDay() { return new Hours(null, null, true); }
    }

    public record DemoProvider(String key, String category, String businessName, String description,
            String firstName, String lastName, String city, String address,
            BigDecimal latitude, BigDecimal longitude) {
        public String email() { return "demo." + key + "@example.com"; }
        public String phone() { return "DEMO - НЕ СЕ БИРА"; }
        public String website() { return "https://example.com/demo/" + key; }
        ProviderRequest request(Long categoryId) {
            return new ProviderRequest(categoryId, businessName, description, address, city,
                    latitude, longitude, phone(), email(), website());
        }
    }
}
