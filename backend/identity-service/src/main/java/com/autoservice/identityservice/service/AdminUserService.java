package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.UpdateAccountStatusRequest;
import com.autoservice.identityservice.dto.response.AdminUserResponse;
import com.autoservice.identityservice.dto.response.PageResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.exception.ResourceNotFoundException;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(
            String keyword,
            Role role,
            AccountStatus accountStatus,
            Pageable pageable
    ) {
        validatePageSize(pageable);

        String normalizedKeyword =
                normalizeKeyword(keyword);

        Page<AdminUserResponse> result =
                userRepository.searchUsers(
                        normalizedKeyword,
                        role,
                        accountStatus,
                        pageable
                ).map(this::toResponse);

        return PageResponse.from(result);
    }
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(
            Long userId
    ) {
        User user = userRepository
                .findByIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.USER_NOT_FOUND,
                                "Không tìm thấy tài khoản."
                        )
                );

        return toResponse(user);
    }
    @Transactional
    public AdminUserResponse updateAccountStatus(
            Long currentAdminId,
            Long targetUserId,
            UpdateAccountStatusRequest request
    ) {
        if (currentAdminId.equals(targetUserId)) {
            throw new BusinessException(
                    ErrorCode.CANNOT_UPDATE_OWN_ACCOUNT,
                    "Admin không được tự thay đổi trạng thái tài khoản của mình."
            );
        }

        User user = userRepository
                .findByIdAndDeletedFalse(targetUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.USER_NOT_FOUND,
                                "Không tìm thấy tài khoản."
                        )
                );

        AccountStatus currentStatus =
                user.getAccountStatus();

        AccountStatus newStatus =
                request.accountStatus();

        if (newStatus == currentStatus) {
            return toResponse(user);
        }

        applyNewStatus(user, newStatus);

        User savedUser =
                userRepository.saveAndFlush(user);

        if (newStatus != AccountStatus.ACTIVE) {
            refreshTokenService.revokeAllForUser(
                    savedUser.getId()
            );
        }

        return toResponse(savedUser);
    }

    private void applyNewStatus(
            User user,
            AccountStatus newStatus
    ) {
        user.setAccountStatus(newStatus);

        if (newStatus == AccountStatus.ACTIVE) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            return;
        }

        if (newStatus != AccountStatus.LOCKED) {
            user.setLockedUntil(null);
        }
    }

    private void validatePageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Số phần tử mỗi trang không được vượt quá 100."
            );
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getAccountStatus(),
                user.getFailedLoginCount(),
                user.getLockedUntil(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}