package com.smartlogistics.service;

import com.smartlogistics.dto.AuthResponse;
import com.smartlogistics.dto.LoginRequest;
import com.smartlogistics.dto.RegisterRequest;
import com.smartlogistics.exception.ConflictException;
import com.smartlogistics.exception.UnauthorizedException;
import com.smartlogistics.model.User;
import com.smartlogistics.repository.BusinessProfileRepository;
import com.smartlogistics.repository.DriverProfileRepository;
import com.smartlogistics.repository.UserRepository;
import com.smartlogistics.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("user123")
                .name("John Driver")
                .email("john@example.com")
                .password("hashedPassword")
                .role("driver")
                .phone("9876543210")
                .isVerified(false)
                .build();
    }

    @Test
    void testRegisterDriverSuccess() {
        RegisterRequest req = RegisterRequest.builder()
                .name("John Driver")
                .email("john@example.com")
                .password("password123")
                .role("driver")
                .phone("9876543210")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateToken("user123", "driver")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("john@example.com", res.getUser().getEmail());
        assertEquals("driver", res.getUser().getRole());
        verify(driverProfileRepository, times(1)).save(any());
    }

    @Test
    void testRegisterDuplicateEmailThrowsConflict() {
        RegisterRequest req = RegisterRequest.builder()
                .name("John Driver")
                .email("john@example.com")
                .password("password123")
                .role("driver")
                .phone("9876543210")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("user123", "driver")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("user123", res.getUser().getId());
    }

    @Test
    void testLoginInvalidPasswordThrowsUnauthorized() {
        LoginRequest req = LoginRequest.builder()
                .email("john@example.com")
                .password("wrongPassword")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }
}
