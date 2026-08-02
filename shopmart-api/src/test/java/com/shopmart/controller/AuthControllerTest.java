package com.shopmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.dto.LoginRequest;
import com.shopmart.dto.RegisterRequest;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_newUser_returns201WithToken() throws Exception {
        RegisterRequest request = new RegisterRequest("fresh@shopmart.com", "password1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("fresh@shopmart.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        userRepository.save(User.builder()
                .email("dup@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        RegisterRequest request = new RegisterRequest("dup@shopmart.com", "password1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidPayload_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        userRepository.save(User.builder()
                .email("login-user@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        LoginRequest request = new LoginRequest("login-user@shopmart.com", "password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("login-user@shopmart.com")));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        userRepository.save(User.builder()
                .email("login-user2@shopmart.com")
                .password(passwordEncoder.encode("password1"))
                .role(Role.CUSTOMER)
                .build());
        LoginRequest request = new LoginRequest("login-user2@shopmart.com", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nonexistentUser_returns401() throws Exception {
        LoginRequest request = new LoginRequest("ghost@shopmart.com", "password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
