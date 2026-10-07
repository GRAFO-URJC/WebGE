package com.gramevapp.config;

import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.UserRepository;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(BootstrapAdminInitializer.class.getName());
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String bootstrapPassword;

    public BootstrapAdminInitializer(UserService userService, UserRepository userRepository,
                                     BCryptPasswordEncoder passwordEncoder,
                                     @Value("${webge.admin.password:}") String bootstrapPassword) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bootstrapPassword.isBlank()) {
            LOGGER.warning("Bootstrap administrator password is not configured; the default admin account remains disabled.");
            return;
        }
        if (bootstrapPassword.length() < 12) {
            throw new IllegalStateException("WEBGE_ADMIN_PASSWORD must contain at least 12 characters.");
        }

        User admin = userService.findByUsername("admin");
        if (admin == null) {
            throw new IllegalStateException("The bootstrap administrator account is missing.");
        }
        if (!passwordEncoder.matches("admin", admin.getPassword())) {
            LOGGER.info("Administrator account already has a non-default password; leaving it unchanged.");
            return;
        }
        admin.setPassword(passwordEncoder.encode(bootstrapPassword));
        admin.setEnabled(true);
        userRepository.save(admin);
    }
}
