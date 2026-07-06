package com.sangam.abcbank.userservice.config;

import com.sangam.abcbank.userservice.model.Role;
import com.sangam.abcbank.userservice.model.User;
import com.sangam.abcbank.userservice.repository.RoleRepository;
import com.sangam.abcbank.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(1L);
        
        userRole = new Role("ROLE_USER");
        userRole.setId(2L);
    }

    @Test
    void testRun_CreatesAdminRoleWhenNotExists() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // Act
        dataSeeder.run();

        // Assert
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(1)).save(roleCaptor.capture());
        assertEquals("ROLE_ADMIN", roleCaptor.getValue().getName());
    }

    @Test
    void testRun_CreatesUserRoleWhenNotExists() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(userRole);
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // Act
        dataSeeder.run();

        // Assert
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(1)).save(roleCaptor.capture());
        assertEquals("ROLE_USER", roleCaptor.getValue().getName());
    }

    @Test
    void testRun_DoesNotCreateRolesWhenTheyExist() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // Act
        dataSeeder.run();

        // Assert
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testRun_CreatesAdminUserWhenNotExists() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("Admin@123")).thenReturn("encoded_admin_password");
        
        User expectedAdmin = User.builder()
                .username("admin")
                .password("encoded_admin_password")
                .email("admin@abcbank.com")
                .fullName("System Administrator")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(expectedAdmin);

        // Act
        dataSeeder.run();

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("admin", savedUser.getUsername());
        assertEquals("admin@abcbank.com", savedUser.getEmail());
        assertEquals("System Administrator", savedUser.getFullName());
        assertTrue(savedUser.isEnabled());
        assertEquals(Set.of(adminRole), savedUser.getRoles());
    }

    @Test
    void testRun_DoesNotCreateAdminUserWhenAlreadyExists() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRun_EncodesAdminPasswordCorrectly() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("Admin@123")).thenReturn("encrypted_password_hash");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        dataSeeder.run();

        // Assert
        verify(passwordEncoder, times(1)).encode("Admin@123");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encrypted_password_hash", userCaptor.getValue().getPassword());
    }

    @Test
    void testRun_CompleteInitializationScenario() {
        // Arrange: Simulate first-time startup - roles don't exist, admin doesn't exist
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class)))
                .thenReturn(adminRole)
                .thenReturn(userRole);
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("Admin@123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        dataSeeder.run();

        // Assert
        // Verify both roles are created
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(2)).save(roleCaptor.capture());
        
        java.util.List<Role> savedRoles = roleCaptor.getAllValues();
        assertTrue(savedRoles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())));
        assertTrue(savedRoles.stream().anyMatch(r -> "ROLE_USER".equals(r.getName())));

        // Verify admin user is created
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("Admin@123");
    }

    @Test
    void testRun_NoArgsConstructor() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // Act & Assert - should not throw any exception
        assertDoesNotThrow(() -> dataSeeder.run());
    }

    @Test
    void testRun_WithArgsConstructor() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // Act & Assert - should not throw any exception
        assertDoesNotThrow(() -> dataSeeder.run("arg1", "arg2"));
    }
}
