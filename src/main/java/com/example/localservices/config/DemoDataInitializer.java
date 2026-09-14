package com.example.localservices.config;

import com.example.localservices.entity.*;
import com.example.localservices.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component
@Profile("local")
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private final UserRepository users; private final CategoryRepository categories; private final ServiceProviderRepository providers;
    private final AvailabilityRepository availability; private final PasswordEncoder encoder;
    public DemoDataInitializer(UserRepository users, CategoryRepository categories, ServiceProviderRepository providers,
            AvailabilityRepository availability, PasswordEncoder encoder) {
        this.users = users; this.categories = categories; this.providers = providers; this.availability = availability; this.encoder = encoder;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        demoUser("Elena", "Petrova", "user@najdiusluga.mk", "+389 70 111 222", "User123!", UserRole.USER);
        User providerOwner = demoUser("Marko", "Stojanov", "provider@najdiusluga.mk", "+389 71 333 444", "Provider123!", UserRole.PROVIDER);
        demoUser("Ana", "Administrator", "admin@najdiusluga.mk", "+389 72 555 666", "Admin123!", UserRole.ADMIN);
        Category category = categories.findBySlugAndActiveTrue("auto-services").orElseThrow();
        ServiceProvider provider = providers.findByOwnerId(providerOwner.getId()).orElseGet(() -> new ServiceProvider(providerOwner, category, "Urban Auto Care"));
        provider.setCategory(category); provider.setBusinessName("Urban Auto Care");
        provider.setDescription("Professional diagnostics, regular maintenance and reliable vehicle repair in Skopje. Transparent estimates, experienced technicians and appointments that respect your time.");
        provider.setAddress("Partizanski Odredi 72"); provider.setCity("Skopje");
        provider.setLatitude(new BigDecimal("42.0042000")); provider.setLongitude(new BigDecimal("21.4105000"));
        provider.setPhone("+389 70 700 900"); provider.setEmail("service@urbanautocare.mk"); provider.setWebsite("https://example.com");
        provider.setVerificationStatus(VerificationStatus.VERIFIED);
        ServiceProvider savedProvider = providers.save(provider);
        for (DayOfWeek day : DayOfWeek.values()) {
            Availability hours = availability.findByServiceProviderIdAndDayOfWeek(savedProvider.getId(), day)
                    .orElseGet(() -> new Availability(savedProvider, day));
            boolean weekend = day == DayOfWeek.SUNDAY; hours.setClosed(weekend);
            hours.setStartTime(weekend ? null : day == DayOfWeek.SATURDAY ? java.time.LocalTime.of(9, 0) : java.time.LocalTime.of(8, 0));
            hours.setEndTime(weekend ? null : day == DayOfWeek.SATURDAY ? java.time.LocalTime.of(14, 0) : java.time.LocalTime.of(17, 0));
            availability.save(hours);
        }
    }

    private User demoUser(String first, String last, String email, String phone, String password, UserRole role) {
        User user = users.findByEmailIgnoreCase(email).orElseGet(() -> new User(first, last, email, encoder.encode(password), role));
        user.setFirstName(first); user.setLastName(last); user.setPhone(phone); user.setRole(role); user.setEnabled(true);
        if (user.getId() != null) user.setPasswordHash(encoder.encode(password));
        return users.save(user);
    }
}
