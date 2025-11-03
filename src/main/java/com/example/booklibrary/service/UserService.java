package com.example.booklibrary.service;

import com.example.booklibrary.model.RefreshToken;
import com.example.booklibrary.model.Role;
import com.example.booklibrary.model.User;
import com.example.booklibrary.repository.RefreshTokenRepository;
import com.example.booklibrary.repository.UserRepository;
import com.example.booklibrary.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // default 7 days
    private Long refreshExpirationMs;

    public UserService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) throw new RuntimeException("Username already exists");
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public AuthResult login(String username, String rawPassword) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = opt.get();
        if (user.isLocked()) {
            throw new RuntimeException("Account locked. Contact admin.");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 3) {
                user.setLocked(true);
                user.setLockTime(java.time.LocalDateTime.now());
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }
        // Successful login: reset failed attempts
        user.setFailedAttempts(0);
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = createRefreshTokenForUser(user);
        return new AuthResult(accessToken, refreshToken);
    }

    private String createRefreshTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        //Instant expiry = Instant.now().plusMillis(refreshExpirationMs);
        Instant expiryDate = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 days from now
        RefreshToken refreshToken = new RefreshToken(token, user, expiryDate);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public String refreshAccessToken(String refreshToken) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new RuntimeException("Refresh token expired");
        }
        User user = rt.getUser();
        return jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    // inner DTO for result
    public static class AuthResult {
        public final String accessToken;
        public final String refreshToken;
        public AuthResult(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
