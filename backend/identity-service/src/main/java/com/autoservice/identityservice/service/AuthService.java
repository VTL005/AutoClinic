package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.RefreshToken;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.LoginRequest;
import com.autoservice.identityservice.dto.request.RefreshTokenRequest;
import com.autoservice.identityservice.dto.request.RegisterRequest;
import com.autoservice.identityservice.dto.response.AuthTokenResponse;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.DuplicateResourceException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    private static final Duration TEMPORARY_LOCK_DURATION =
            Duration.ofMinutes(15);

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username =
                normalizeUsername(request.username());

        String phone = request.phone().trim();
        String email = normalizeEmail(request.email());

        validateUniqueInformation(
                username,
                phone,
                email
        );

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFullName(request.fullName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        user.setAccountStatus(
                AccountStatus.PENDING_ACTIVATION
        );

        User savedUser =
                userRepository.saveAndFlush(user);

        return toUserResponse(savedUser);
    }

    @Transactional(
            noRollbackFor = BusinessException.class
    )
    public AuthTokenResponse login(LoginRequest request) {
        String username =
                normalizeUsername(request.username());

        User user = userRepository
                .findByUsernameIgnoreCaseAndDeletedFalse(
                        username
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_CREDENTIALS,
                                "Tên đăng nhập hoặc mật khẩu không đúng."
                        )
                );

        releaseTemporaryLockIfExpired(user);
        ensureAccountIsNotLocked(user);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            handleFailedLogin(user);

            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Tên đăng nhập hoặc mật khẩu không đúng."
            );
        }

        ensureAccountCanAuthenticate(user);

        resetFailedLoginState(user);
        userRepository.save(user);

        return issueTokenPair(user);
    }

    @Transactional
    public AuthTokenResponse refresh(
            RefreshTokenRequest request
    ) {
        RefreshToken currentRefreshToken =
                refreshTokenService.requireUsable(
                        request.refreshToken()
                );

        User user = currentRefreshToken.getUser();

        releaseTemporaryLockIfExpired(user);
        ensureAccountIsNotLocked(user);
        ensureAccountCanAuthenticate(user);

        refreshTokenService.revoke(
                currentRefreshToken
        );

        return issueTokenPair(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(
                request.refreshToken()
        );
    }

    private AuthTokenResponse issueTokenPair(User user) {
        String accessToken =
                jwtService.generateAccessToken(user);

        RefreshTokenService.IssuedRefreshToken
                issuedRefreshToken =
                refreshTokenService.issue(user);

        return new AuthTokenResponse(
                accessToken,
                issuedRefreshToken.token(),
                TOKEN_TYPE,
                jwtService.getAccessTokenExpiresInSeconds(),
                toUserResponse(user)
        );
    }

    private void handleFailedLogin(User user) {
        int currentFailedCount =
                user.getFailedLoginCount() == null
                        ? 0
                        : user.getFailedLoginCount();

        int newFailedCount = currentFailedCount + 1;

        user.setFailedLoginCount(newFailedCount);

        if (newFailedCount >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setAccountStatus(
                    AccountStatus.LOCKED
            );

            user.setLockedUntil(
                    Instant.now().plus(
                            TEMPORARY_LOCK_DURATION
                    )
            );
        }

        userRepository.save(user);
    }

    private void releaseTemporaryLockIfExpired(
            User user
    ) {
        if (user.getAccountStatus()
                != AccountStatus.LOCKED) {
            return;
        }

        Instant lockedUntil = user.getLockedUntil();

        if (lockedUntil != null
                && !lockedUntil.isAfter(Instant.now())) {
            user.setAccountStatus(
                    AccountStatus.ACTIVE
            );
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    private void ensureAccountIsNotLocked(
            User user
    ) {
        if (user.getAccountStatus()
                == AccountStatus.LOCKED) {

            String message =
                    user.getLockedUntil() == null
                            ? "Tài khoản đã bị khóa."
                            : "Tài khoản đang bị khóa tạm thời.";

            throw new BusinessException(
                    ErrorCode.ACCOUNT_LOCKED,
                    message
            );
        }
    }

    private void ensureAccountCanAuthenticate(
            User user
    ) {
        if (user.getAccountStatus()
                == AccountStatus.PENDING_ACTIVATION) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_PENDING_ACTIVATION,
                    "Tài khoản đang chờ kích hoạt."
            );
        }

        if (user.getAccountStatus()
                == AccountStatus.DISABLED) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED,
                    "Tài khoản đã bị vô hiệu hóa."
            );
        }

        if (user.getAccountStatus()
                != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "Tài khoản không được phép đăng nhập."
            );
        }
    }

    private void resetFailedLoginState(User user) {
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
    }

    private void validateUniqueInformation(
            String username,
            String phone,
            String email
    ) {
        if (userRepository
                .existsByUsernameIgnoreCase(username)) {
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
                && userRepository
                .existsByEmailIgnoreCase(email)) {
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