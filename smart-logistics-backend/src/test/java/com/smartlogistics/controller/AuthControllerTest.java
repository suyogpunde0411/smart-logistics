package com.smartlogistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogistics.dto.AuthResponse;
import com.smartlogistics.dto.LoginRequest;
import com.smartlogistics.dto.RegisterRequest;
import com.smartlogistics.dto.UserSummaryDto;
import com.smartlogistics.security.JwtAuthenticationFilter;
import com.smartlogistics.security.JwtTokenProvider;
import com.smartlogistics.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testRegisterEndpointReturns201() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .name("Driver Dave")
                .email("dave@driver.com")
                .password("secret123")
                .role("driver")
                .phone("9876543210")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("sample.jwt.token")
                .user(UserSummaryDto.builder()
                        .id("user99")
                        .name("Driver Dave")
                        .email("dave@driver.com")
                        .role("driver")
                        .build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("sample.jwt.token"))
                .andExpect(jsonPath("$.data.user.email").value("dave@driver.com"))
                .andExpect(jsonPath("$.data.user._id").value("user99"));
    }

    @Test
    void testLoginEndpointReturns200() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("dave@driver.com")
                .password("secret123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("sample.jwt.token")
                .user(UserSummaryDto.builder()
                        .id("user99")
                        .name("Driver Dave")
                        .email("dave@driver.com")
                        .role("driver")
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("sample.jwt.token"))
                .andExpect(jsonPath("$.data.user.id").value("user99"));
    }
}
