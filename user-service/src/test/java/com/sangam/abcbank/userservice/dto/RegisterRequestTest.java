package com.sangam.abcbank.userservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterRequest DTO Tests")
class RegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Valid Registration Tests ====================

    @Test
    @DisplayName("Should create RegisterRequest with valid data")
    void testValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("john@example.com");
        request.setFullName("John Doe");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid register request should have no violations");
    }

    @Test
    @DisplayName("Should accept minimum username length (3 characters)")
    void testUsernameMinimumLength() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abc");
        request.setPassword("password123");
        request.setEmail("user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Username with 3 characters should be valid");
    }

    @Test
    @DisplayName("Should accept maximum username length (50 characters)")
    void testUsernameMaximumLength() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(50));
        request.setPassword("password123");
        request.setEmail("user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Username with 50 characters should be valid");
    }

    @Test
    @DisplayName("Should accept password with minimum length (6 characters)")
    void testPasswordMinimumLength() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("123456");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Password with 6 characters should be valid");
    }

    @Test
    @DisplayName("Should accept various valid email formats")
    void testValidEmailFormats() {
        String[] validEmails = {
                "user@example.com",
                "john.doe@company.co.uk",
                "test+tag@domain.org",
                "user123@test-domain.com"
        };

        for (String email : validEmails) {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("johndoe");
            request.setPassword("password123");
            request.setEmail(email);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Email '" + email + "' should be valid");
        }
    }

    @Test
    @DisplayName("Should accept RegisterRequest without roles")
    void testRegisterRequestWithoutRoles() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("john@example.com");
        request.setRoles(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "RegisterRequest without roles should be valid");
    }

    @Test
    @DisplayName("Should accept RegisterRequest with custom roles")
    void testRegisterRequestWithRoles() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("john@example.com");
        request.setRoles(Set.of("ROLE_USER"));

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "RegisterRequest with roles should be valid");
    }

    @Test
    @DisplayName("Should accept RegisterRequest without fullName")
    void testRegisterRequestWithoutFullName() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("john@example.com");
        request.setFullName(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "RegisterRequest without fullName should be valid");
    }

    // ==================== Invalid Username Tests ====================

    @Test
    @DisplayName("Should reject blank username")
    void testBlankUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setPassword("password123");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank username should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("username is required")),
                "Should have 'username is required' violation");
    }

    @Test
    @DisplayName("Should reject null username")
    void testNullUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(null);
        request.setPassword("password123");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null username should have violations");
    }

    @Test
    @DisplayName("Should reject username with less than 3 characters")
    void testUsernameUnderMinimumLength() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");
        request.setPassword("password123");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Username with 2 characters should have violations");
    }

    @Test
    @DisplayName("Should reject username exceeding maximum length (50 characters)")
    void testUsernameExceedingMaximumLength() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(51));
        request.setPassword("password123");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Username with 51 characters should have violations");
    }

    // ==================== Invalid Password Tests ====================

    @Test
    @DisplayName("Should reject blank password")
    void testBlankPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank password should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("password is required")),
                "Should have 'password is required' violation");
    }

    @Test
    @DisplayName("Should reject null password")
    void testNullPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword(null);
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null password should have violations");
    }

    @Test
    @DisplayName("Should reject password with less than 6 characters")
    void testPasswordUnderMinimumLength() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("12345");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Password with 5 characters should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("password must be at least 6 characters")),
                "Should have 'password must be at least 6 characters' violation");
    }

    // ==================== Invalid Email Tests ====================

    @Test
    @DisplayName("Should reject blank email")
    void testBlankEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank email should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("email is required")),
                "Should have 'email is required' violation");
    }

    @Test
    @DisplayName("Should reject null email")
    void testNullEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null email should have violations");
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void testInvalidEmailFormats() {
        String[] invalidEmails = {
                "plainaddress",
                "@nodomain.com",
                "user@",
                "user name@example.com",
                "user@domain",
                "user@@example.com"
        };

        for (String email : invalidEmails) {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("johndoe");
            request.setPassword("password123");
            request.setEmail(email);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty(), "Email '" + email + "' should be invalid");
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("email must be valid")),
                    "Email '" + email + "' should have 'email must be valid' violation");
        }
    }

    // ==================== Multiple Violations Tests ====================

    @Test
    @DisplayName("Should report multiple violations for all invalid fields")
    void testMultipleViolations() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");
        request.setPassword("123");
        request.setEmail("invalid-email");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.size() >= 3, "Should have violations for all invalid fields");
    }

    @Test
    @DisplayName("Should reject all blank required fields")
    void testAllBlankRequiredFields() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setPassword("");
        request.setEmail("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "All blank fields should have violations");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle username with numbers and special characters")
    void testUsernameWithSpecialCharacters() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user_123-test");
        request.setPassword("password123");
        request.setEmail("user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Username with numbers and underscores should be valid");
    }

    @Test
    @DisplayName("Should handle password with special characters")
    void testPasswordWithSpecialCharacters() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("P@ssw0rd!@#");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Password with special characters should be valid");
    }

    @Test
    @DisplayName("Should handle long email addresses")
    void testLongEmailAddress() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("very.long.email.address.with.many.dots@subdomain.example.co.uk");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Long email address should be valid");
    }

    @Test
    @DisplayName("Should handle whitespace-only username")
    void testWhitespaceOnlyUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("   ");
        request.setPassword("password123");
        request.setEmail("john@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Whitespace-only username should have violations");
    }

    @Test
    @DisplayName("Should handle case-insensitive email validation")
    void testCaseInsensitiveEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("JOHN@EXAMPLE.COM");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Uppercase email should be valid");
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    @DisplayName("Should correctly set and get all fields")
    void testGettersAndSetters() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setPassword("password123");
        request.setEmail("john@example.com");
        request.setFullName("John Doe");
        request.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        assertEquals("johndoe", request.getUsername());
        assertEquals("password123", request.getPassword());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("John Doe", request.getFullName());
        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN"), request.getRoles());
    }
}
