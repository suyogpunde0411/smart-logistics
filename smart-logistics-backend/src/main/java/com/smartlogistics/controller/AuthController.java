package com.smartlogistics.controller;

import com.smartlogistics.dto.ApiResponse;
import com.smartlogistics.dto.AuthResponse;
import com.smartlogistics.dto.LoginRequest;
import com.smartlogistics.dto.RegisterRequest;
import com.smartlogistics.model.User;
import com.smartlogistics.security.CustomUserDetails;
import com.smartlogistics.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userDetails.getUser()));
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'admin')")
    public ResponseEntity<ApiResponse<Void>> adminOnly() {
        return ResponseEntity.ok(ApiResponse.successMessage("Welcome admin"));
    }
}
