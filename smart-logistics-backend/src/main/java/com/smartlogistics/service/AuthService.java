package com.smartlogistics.service;

import com.smartlogistics.dto.AuthResponse;
import com.smartlogistics.dto.LoginRequest;
import com.smartlogistics.dto.RegisterRequest;
import com.smartlogistics.dto.UserSummaryDto;
import com.smartlogistics.exception.ConflictException;
import com.smartlogistics.exception.UnauthorizedException;
import com.smartlogistics.model.BusinessProfile;
import com.smartlogistics.model.DriverProfile;
import com.smartlogistics.model.User;
import com.smartlogistics.repository.BusinessProfileRepository;
import com.smartlogistics.repository.DriverProfileRepository;
import com.smartlogistics.repository.UserRepository;
import com.smartlogistics.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        User user = User.builder()
                .name(req.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole().toLowerCase().trim())
                .phone(req.getPhone().trim())
                .isVerified(false)
                .build();

        user = userRepository.save(user);

        if ("driver".equalsIgnoreCase(user.getRole())) {
            DriverProfile profile = DriverProfile.builder()
                    .user(user.getId())
                    .build();
            driverProfileRepository.save(profile);
        } else if ("business".equalsIgnoreCase(user.getRole())) {
            BusinessProfile profile = BusinessProfile.builder()
                    .user(user.getId())
                    .companyName(user.getName())
                    .build();
            businessProfileRepository.save(profile);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .isVerified(user.getIsVerified())
                .build();

        return AuthResponse.builder()
                .user(userSummary)
                .token(token)
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .isVerified(user.getIsVerified())
                .build();

        return AuthResponse.builder()
                .user(userSummary)
                .token(token)
                .build();
    }
}
