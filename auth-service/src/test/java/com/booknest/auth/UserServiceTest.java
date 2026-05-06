package com.booknest.auth;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.security.JwtUtil;
import com.booknest.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private BCryptPasswordEncoder realEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realEncoder = new BCryptPasswordEncoder();
    }

    // ---- Register ----
    @Test
    void testRegisterUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("rawPassword"); // controller sets passwordHash before calling service

        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash(realEncoder.encode("rawPassword"));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(user);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        // Password should be hashed after register
        assertNotEquals("rawPassword", result.getPasswordHash());
    }

    // ---- Authenticate - Valid Credentials ----
    @Test
    void testAuthenticate_Success() {
        String rawPassword = "rawPassword";
        String encoded = realEncoder.encode(rawPassword);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash(encoded);
        user.setRole("CUSTOMER");

        when(userRepository.findAllByEmail("test@example.com")).thenReturn(List.of(user));

        User result = authService.authenticate("test@example.com", rawPassword);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("CUSTOMER", result.getRole());
    }

    // ---- Authenticate - Wrong Password ----
    @Test
    void testAuthenticate_WrongPassword_ReturnsNull() {
        String encoded = realEncoder.encode("correctPassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash(encoded);

        when(userRepository.findAllByEmail("test@example.com")).thenReturn(List.of(user));

        User result = authService.authenticate("test@example.com", "wrongPassword");

        assertNull(result);
    }

    // ---- Authenticate - User Not Found ----
    @Test
    void testAuthenticate_UserNotFound_ReturnsNull() {
        when(userRepository.findAllByEmail(anyString())).thenReturn(List.of());

        User result = authService.authenticate("nobody@example.com", "anyPassword");

        assertNull(result);
    }

    // ---- Change Password ----
    @Test
    void testChangePassword_Success() {
        User user = new User();
        user.setPasswordHash(realEncoder.encode("oldPassword"));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.changePassword(1, "newPassword");

        verify(userRepository, times(1)).save(any(User.class));
        // Verify new password was hashed and set
        assertTrue(realEncoder.matches("newPassword", user.getPasswordHash()));
    }

    // ---- Change Password - User Not Found ----
    @Test
    void testChangePassword_UserNotFound_ThrowsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.changePassword(99, "newPass"));
    }

    // ---- Change Mobile ----
    @Test
    void testChangeMobile_Success() {
        User user = new User();
        user.setMobile(1111111111L);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.changeMobile(1, 9876543210L);

        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(9876543210L, user.getMobile());
    }

    // ---- Validate Token ----
    @Test
    void testValidateToken_Valid() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        assertTrue(authService.validateToken("valid-token"));
    }

    @Test
    void testValidateToken_Invalid() {
        when(jwtUtil.validateToken("bad-token")).thenReturn(false);
        assertFalse(authService.validateToken("bad-token"));
    }
}
