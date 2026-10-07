package com.gramevapp.config;

import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.UserRepository;
import com.gramevapp.web.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BootstrapAdminInitializerTest {

    private final UserService userService = mock(UserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);
    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin"));
        admin.setEnabled(false);
        when(userService.findByUsername("admin")).thenReturn(admin);
    }

    @Test
    void leavesSeededAccountDisabledWhenNoPasswordIsConfigured() {
        initializer("").run(new DefaultApplicationArguments());

        assertFalse(admin.getEnabled());
        verify(userRepository, never()).save(admin);
    }

    @Test
    void enablesSeededAccountWithConfiguredPassword() {
        initializer("LongAndSecureAdminPassword").run(new DefaultApplicationArguments());

        assertTrue(admin.getEnabled());
        assertTrue(encoder.matches("LongAndSecureAdminPassword", admin.getPassword()));
        verify(userRepository).save(admin);
    }

    @Test
    void rejectsConfiguredPasswordThatIsTooShort() {
        assertThrows(IllegalStateException.class,
                () -> initializer("short").run(new DefaultApplicationArguments()));
        verify(userService, never()).findByUsername("admin");
    }

    @Test
    void doesNotReplaceAnAdminPasswordThatWasAlreadyChanged() {
        admin.setPassword(encoder.encode("PreviouslyChangedPassword"));

        initializer("NewConfiguredAdminPassword").run(new DefaultApplicationArguments());

        assertTrue(encoder.matches("PreviouslyChangedPassword", admin.getPassword()));
        assertFalse(encoder.matches("NewConfiguredAdminPassword", admin.getPassword()));
        verify(userRepository, never()).save(admin);
    }

    @Test
    void failsExplicitlyWhenSeedAccountIsMissing() {
        when(userService.findByUsername("admin")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> initializer("LongAndSecureAdminPassword").run(new DefaultApplicationArguments()));
        assertEquals("The bootstrap administrator account is missing.", exception.getMessage());
    }

    private BootstrapAdminInitializer initializer(String password) {
        return new BootstrapAdminInitializer(userService, userRepository, encoder, password);
    }
}
