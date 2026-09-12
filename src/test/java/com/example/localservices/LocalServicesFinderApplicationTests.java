package com.example.localservices;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.localservices.repository.UserRepository;
import com.example.localservices.repository.ServiceProviderRepository;
import com.example.localservices.repository.CategoryRepository;
import com.example.localservices.repository.AvailabilityRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration")
class LocalServicesFinderApplicationTests {
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean ServiceProviderRepository serviceProviderRepository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean AvailabilityRepository availabilityRepository;

    @Test
    void applicationContextLoads() {
    }
}
