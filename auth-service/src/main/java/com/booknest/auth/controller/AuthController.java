package com.booknest.auth.controller;

import com.booknest.auth.dto.AuthResponse;
import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.RegisterRequest;
import com.booknest.auth.entity.User;
import com.booknest.auth.service.AuthService;
import com.booknest.auth.security.JwtUtil;
import com.booknest.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired 
    private AuthService authService;

    @Autowired 
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // ---------------- CUSTOMER ----------------
    @PostMapping("/customer/register")
    public ResponseEntity<User> registerCustomer(@RequestBody RegisterRequest req) {
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(req.getPassword());
        user.setRole("CUSTOMER");   // fixed role
        user.setMobile(req.getMobile());
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/customer/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid customer credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUserId(), user.getFullName(), user.getEmail(), user.getRole()));
    }

    // ---------------- ADMIN ----------------
    @PostMapping("/admin/register")
    public ResponseEntity<User> registerAdmin(@RequestBody RegisterRequest req) {
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(req.getPassword());
        user.setRole("ADMIN");   // fixed role
        user.setMobile(req.getMobile());
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid admin credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUserId(), user.getFullName(), user.getEmail(), user.getRole()));
    }

    // ---------------- COMMON FEATURES ---------------
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }
        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findTopByEmailOrderByUserIdDesc(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<String> changePassword(@PathVariable int id, @RequestBody String newPassword) {
        authService.changePassword(id, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }

    @PutMapping("/change-mobile/{id}")
    public ResponseEntity<String> changeMobile(@PathVariable int id, @RequestBody Long mobile) {
        authService.changeMobile(id, mobile);
        return ResponseEntity.ok("Mobile number updated successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody com.booknest.auth.dto.ResetPasswordRequest req) {
        authService.resetPassword(req.getEmail(), req.getMobile(), req.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    // ---------------- USER MANAGEMENT (ADMIN ONLY) ----------------
    @GetMapping("/user/all")
    public ResponseEntity<java.util.List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/user/role/{role}")
    public ResponseEntity<java.util.List<User>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userRepository.findAll().stream()
            .filter(u -> u.getRole().equalsIgnoreCase(role))
            .collect(java.util.stream.Collectors.toList()));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable int userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
