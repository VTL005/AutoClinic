package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.dto.request.ChangePasswordRequest;
import com.autoservice.identityservice.dto.request.UpdateProfileRequest;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.DuplicateResourceException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.exception.ResourceNotFoundException;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = getActiveUser(userId);

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(
            Long userId,
            UpdateProfileRequest request
    ) {
        User user = getActiveUser(userId);

        boolean changed = false;

        if (request.fullName() != null) {
            String fullName = request.fullName().trim();

            if (!Objects.equals(
                    user.getFullName(),
                    fullName
            )) {
                user.setFullName(fullName);
                changed = true;
            }
        }

        if (request.phone() != null) {
            String phone = request.phone().trim();

            if (!Objects.equals(
                    user.getPhone(),
                    phone
            )) {
                if (userRepository.existsByPhone(phone)) {
                    throw new DuplicateResourceException(
                            ErrorCode.PHONE_ALREADY_EXISTS,
                            "Số điện thoại đã được sử dụng."
                    );
                }

                user.setPhone(phone);
                user.setPhoneVerifiedAt(null);
                changed = true;
            }
        }

        if (request.email() != null) {
            String email = normalizeEmail(
                    request.email()
            );

            if (!Objects.equals(
                    user.getEmail(),
                    email
            )) {
                if (email != null
                        && userRepository
                        .existsByEmailIgnoreCase(email)) {
                    throw new DuplicateResourceException(
                            ErrorCode.EMAIL_ALREADY_EXISTS,
                            "Email đã được sử dụng."
                    );
                }

                user.setEmail(email);
                user.setEmailVerifiedAt(null);
                changed = true;
            }
        }

        if (!changed) {
            throw new BusinessException(
                    ErrorCode.PROFILE_UPDATE_NO_CHANGES,
                    "Không có thông tin nào thay đổi."
            );
        }

        User savedUser =
                userRepository.saveAndFlush(user);

        return toUserResponse(savedUser);
    }

    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {
        User user = getActiveUser(userId);

        if (!request.newPassword().equals(
                request.confirmPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.PASSWORD_CONFIRMATION_MISMATCH,
                    "Xác nhận mật khẩu không khớp."
            );
        }

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new BusinessException(
                    ErrorCode.CURRENT_PASSWORD_INCORRECT,
                    "Mật khẩu hiện tại không chính xác."
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash()
        )) {
            throw new BusinessException(
                    ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT,
                    "Mật khẩu mới không được trùng mật khẩu hiện tại."
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        userRepository.saveAndFlush(user);

        refreshTokenService.revokeAllForUser(userId);
    }

    private User getActiveUser(Long userId) {
        return userRepository
                .findByIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.USER_NOT_FOUND,
                                "Không tìm thấy người dùng."
                        )
                );
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = email.trim();

        if (normalizedEmail.isEmpty()) {
            return null;
        }

        return normalizedEmail.toLowerCase();
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