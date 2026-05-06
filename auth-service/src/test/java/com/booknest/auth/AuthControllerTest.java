package com.booknest.auth;

import com.booknest.auth.controller.AuthController;
import com.booknest.auth.dto.AuthResponse;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.security.JwtUtil;
import com.booknest.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    // ---- Customer Register ----
    @Test
    public void testRegisterCustomer_Success() throws Exception {
        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setFullName("Test User");
        savedUser.setRole("CUSTOMER");

        when(authService.register(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/auth/customer/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\",\"fullName\":\"Test User\",\"mobile\":9876543210}"))
                .andExpect(status().isOk());
    }

    // ---- Customer Login ----
    @Test
    public void testLoginCustomer_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setRole("CUSTOMER");
        user.setFullName("Test User");

        when(authService.authenticate("test@example.com", "password123")).thenReturn(user);
        when(jwtUtil.generateToken("test@example.com", "CUSTOMER")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/customer/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    // ---- Customer Login - Wrong Role ----
    @Test
    public void testLoginCustomer_WrongRole_Unauthorized() throws Exception {
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");

        when(authService.authenticate("admin@example.com", "password123")).thenReturn(adminUser);

        mockMvc.perform(post("/auth/customer/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---- Admin Login ----
    @Test
    public void testLoginAdmin_Success() throws Exception {
        User admin = new User();
        admin.setEmail("admin@booknest.com");
        admin.setRole("ADMIN");
        admin.setFullName("Admin User");

        when(authService.authenticate("admin@booknest.com", "adminPass")).thenReturn(admin);
        when(jwtUtil.generateToken("admin@booknest.com", "ADMIN")).thenReturn("admin-jwt-token");

        mockMvc.perform(post("/auth/admin/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@booknest.com\",\"password\":\"adminPass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-jwt-token"));
    }

    // ---- Admin Login - Bad Credentials ----
    @Test
    public void testLoginAdmin_NullUser_Unauthorized() throws Exception {
        when(authService.authenticate(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/auth/admin/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---- GET /auth/me - Missing Token ----
    @Test
    public void testMe_MissingToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---- GET /auth/me - Invalid Token ----
    @Test
    public void testMe_InvalidToken_Unauthorized() throws Exception {
        when(jwtUtil.validateToken("bad-token")).thenReturn(false);

        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    // ---- GET /auth/me - Valid Token ----
    @Test
    @WithMockUser
    public void testMe_ValidToken_ReturnsUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole("CUSTOMER");

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("valid-token")).thenReturn("test@example.com");
        when(userRepository.findTopByEmailOrderByUserIdDesc("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
