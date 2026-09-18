package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.RegisterRequest;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.exception.DuplicateResourceException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String username = normalizeUsername(request.username());
        String phone = request.phone().trim();
        String email = normalizeEmail(request.email());

        validateUniqueInformation(username, phone, email);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFullName(request.fullName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        user.setAccountStatus(AccountStatus.PENDING_ACTIVATION);

        User savedUser = userRepository.saveAndFlush(user);

        return toUserResponse(savedUser);
    }

    private void validateUniqueInformation(
            String username,
            String phone,
            String email
    ) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException(
                    ErrorCode.USERNAME_ALREADY_EXISTS,
                    "Tên đăng nhập đã tồn tại."
            );
        }

        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException(
                    ErrorCode.PHONE_ALREADY_EXISTS,
                    "Số điện thoại đã được sử dụng."
            );
        }

        if (email != null
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email đã được sử dụng."
            );
        }
    }

    private String normalizeUsername(String username) {
        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }
}