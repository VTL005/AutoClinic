package com.autoservice.identityservice.config;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.bootstrap-admin",
        name = "enabled",
        havingValue = "true"
)
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username}")
    private String username;

    @Value("${app.bootstrap-admin.password}")
    private String password;

    @Value("${app.bootstrap-admin.full-name}")
    private String fullName;

    @Value("${app.bootstrap-admin.phone}")
    private String phone;

    @Value("${app.bootstrap-admin.email}")
    private String email;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        validateConfiguration();

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(
                normalizedUsername
        )) {
            LOGGER.info(
                    "Bootstrap Admin '{}' already exists. Skipping.",
                    normalizedUsername
            );
            return;
        }

        validateUniqueContactInformation();

        User admin = new User();
        admin.setUsername(normalizedUsername);
        admin.setPasswordHash(
                passwordEncoder.encode(password)
        );
        admin.setFullName(fullName.trim());
        admin.setPhone(phone.trim());
        admin.setEmail(normalizeEmail(email));
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.saveAndFlush(admin);

        LOGGER.info(
                "Bootstrap Admin '{}' was created successfully.",
                normalizedUsername
        );
    }

    private void validateConfiguration() {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_USERNAME must not be blank"
            );
        }

        if (password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD must contain at least 12 characters"
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_FULL_NAME must not be blank"
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PHONE must not be blank"
            );
        }
    }

    private void validateUniqueContactInformation() {
        String normalizedPhone = phone.trim();
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new IllegalStateException(
                    "Bootstrap Admin phone already exists"
            );
        }

        if (normalizedEmail != null
                && userRepository.existsByEmailIgnoreCase(
                normalizedEmail
        )) {
            throw new IllegalStateException(
                    "Bootstrap Admin email already exists"
            );
        }
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}