package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.exception.ResourceNotFoundException;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository
                .findByIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.USER_NOT_FOUND,
                                "Không tìm thấy người dùng."
                        )
                );

        return toUserResponse(user);
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