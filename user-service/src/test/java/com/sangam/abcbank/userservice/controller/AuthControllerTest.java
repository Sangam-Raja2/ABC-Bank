package com.sangam.abcbank.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangam.abcbank.userservice.dto.JwtResponse;
import com.sangam.abcbank.userservice.dto.LoginRequest;
import com.sangam.abcbank.userservice.dto.RegisterRequest;
import com.sangam.abcbank.userservice.dto.UserResponse;
import com.sangam.abcbank.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        // Setup RegisterRequest
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("Password@123");

        // Setup LoginRequest
        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("Password@123");

        // Setup UserResponse
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setEmail("john.doe@example.com");

        // Setup JwtResponse
        jwtResponse = new JwtResponse();
        jwtResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        jwtResponse.setType("Bearer");
        jwtResponse.setExpiresIn(3600L);
    }

    // ==================== REGISTER ENDPOINT TESTS ====================

    @Test
    @DisplayName("Should register user successfully with valid request")
    void testRegisterSuccess() throws Exception {
        // Arrange
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when firstName is missing")
    void testRegisterWithMissingFirstName() throws Exception {
        // Arrange
        registerRequest.setFirstName(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when lastName is missing")
    void testRegisterWithMissingLastName() throws Exception {
        // Arrange
        registerRequest.setLastName(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is missing")
    void testRegisterWithMissingEmail() throws Exception {
        // Arrange
        registerRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password is missing")
    void testRegisterWithMissingPassword() throws Exception {
        // Arrange
        registerRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    void testRegisterWithInvalidEmailFormat() throws Exception {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is empty")
    void testRegisterWithEmptyBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when content type is not JSON")
    void testRegisterWithInvalidContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    // ==================== LOGIN ENDPOINT TESTS ====================

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Arrange
        when(userService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(3600)));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is missing in login")
    void testLoginWithMissingEmail() throws Exception {
        // Arrange
        loginRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password is missing in login")
    void testLoginWithMissingPassword() throws Exception {
        // Arrange
        loginRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid in login")
    void testLoginWithInvalidEmailFormat() throws Exception {
        // Arrange
        loginRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login request body is empty")
    void testLoginWithEmptyBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login content type is not JSON")
    void testLoginWithInvalidContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when both email and password are missing")
    void testLoginWithMissingBoth() throws Exception {
        // Arrange
        loginRequest.setEmail(null);
        loginRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    // ==================== HTTP METHOD TESTS ====================

    @Test
    @DisplayName("Should return 405 Method Not Allowed for GET on /register")
    void testRegisterWithGetMethod() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed for GET on /login")
    void testLoginWithGetMethod() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        verify(userService, never()).login(any(LoginRequest.class));
    }

}
