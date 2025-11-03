package com.example.booklibrary.controller;

import com.example.booklibrary.dto.AuthRequest;
import com.example.booklibrary.dto.AuthResponse;
import com.example.booklibrary.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        var user = userService.register(req.getUsername(), req.getPassword());
        return ResponseEntity.ok().body("User created: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        var result = userService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(new AuthResponse(result.accessToken, result.refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody java.util.Map<String,String> body) {
        String r = body.get("refreshToken");
        String newAccess = userService.refreshAccessToken(r);
        return ResponseEntity.ok(new AuthResponse(newAccess, r));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody java.util.Map<String,String> body) {
        String r = body.get("refreshToken");
        userService.logout(r);
        return ResponseEntity.ok("Logged out");
    }
}
