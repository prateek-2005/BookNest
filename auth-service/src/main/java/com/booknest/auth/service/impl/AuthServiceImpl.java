package com.booknest.auth.service.impl;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.security.JwtUtil;
import com.booknest.auth.service.AuthService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#email")
    public User authenticate(String email, String password) {
        List<User> users = userRepository.findAllByEmail(email);
        for (User user : users) {
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }


    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            redisTemplate.delete("session:" + jwt);
        }
    }

    public String refreshToken(String token) {
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);
            // Ideally fetch role from DB again
            User user = userRepository.findTopByEmailOrderByUserIdDesc(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return jwtUtil.generateToken(email, user.getRole());
        }
        throw new RuntimeException("Invalid token");
    }

    @CacheEvict(value = "users", key = "#id")
    public void changePassword(int id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void resetPassword(String email, Long mobile, String newPassword) {
        User user = userRepository.findTopByEmailOrderByUserIdDesc(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if (user.getMobile() == null || !user.getMobile().equals(mobile)) {
            throw new RuntimeException("Mobile number verification failed");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeMobile(int id, Long mobile) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setMobile(mobile);
        userRepository.save(user);
    }


    public boolean validateToken(String token) {
        boolean isValid = jwtUtil.validateToken(token);
        if (isValid) {
            // Check if session exists in Redis
            return Boolean.TRUE.equals(redisTemplate.hasKey("session:" + token));
        }
        return false;
    }

    public void createSession(String token, String email) {
        // Store session in Redis for 1 hour (matching JWT expiration)
        redisTemplate.opsForValue().set("session:" + token, email, 1, java.util.concurrent.TimeUnit.HOURS);
    }

    @Override
    public void sendOtp(String email) {
        // 1. Generate 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
        
        // 2. Store in Redis with 5-minute TTL
        redisTemplate.opsForValue().set("otp:" + email, otp, 5, java.util.concurrent.TimeUnit.MINUTES);
        
        // 3. Send via Notification Service
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("userId", 0); // System notification
            payload.put("type", "EMAIL_VERIFICATION");
            payload.put("message", "Your BookNest verification code is: " + otp);
            payload.put("channel", "EMAIL");
            payload.put("recipientEmail", email);
            
            restTemplate.postForEntity("http://NOTIFICATION-SERVICE/notifications", payload, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("otp:" + email);
        return otp != null && otp.equals(storedOtp);
    }
}
