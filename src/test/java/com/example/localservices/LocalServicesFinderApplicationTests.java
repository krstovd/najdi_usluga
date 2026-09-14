package com.example.localservices;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.localservices.repository.UserRepository;
import com.example.localservices.repository.ServiceProviderRepository;
import com.example.localservices.repository.CategoryRepository;
import com.example.localservices.repository.AvailabilityRepository;
import com.example.localservices.repository.ReservationRepository;
import com.example.localservices.repository.ReviewRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "app.demo-data.enabled=false"
})
@AutoConfigureMockMvc
class LocalServicesFinderApplicationTests {
    @Autowired MockMvc mockMvc;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean ServiceProviderRepository serviceProviderRepository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean AvailabilityRepository availabilityRepository;
    @MockitoBean ReservationRepository reservationRepository;
    @MockitoBean ReviewRepository reviewRepository;

    @Test
    void applicationContextLoads() {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorCanAccessStatistics() throws Exception {
        mockMvc.perform(get("/api/admin/statistics")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotAccessAdministration() throws Exception {
        mockMvc.perform(get("/api/admin/statistics")).andExpect(status().isForbidden());
    }

    @Test
    void loginAndRegistrationPagesArePublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void ownAvailabilityIsPrivateButNumericProviderAvailabilityIsPublic() throws Exception {
        mockMvc.perform(get("/api/providers/me/availability")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/providers/123/availability")).andExpect(status().isNotFound());
    }
}
