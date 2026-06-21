package org.services.fooddeliveryservice.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.UserRole;
import org.services.fooddeliveryservice.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    AppUserRepository users;
    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        users.deleteAll();
        users.save(new AppUser("Customer", "security-customer", encoder.encode("pw"), UserRole.CUSTOMER));
    }

    @Test
    void customerCannotAccessAdminApi() throws Exception {
        mockMvc.perform(post("/api/admin/cities")
                        .with(httpBasic("security-customer", "pw"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Blocked City\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotPlaceOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
