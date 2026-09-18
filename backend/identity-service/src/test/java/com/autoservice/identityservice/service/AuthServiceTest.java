package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.RegisterRequest;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.exception.DuplicateResourceException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WithValidInformation_ShouldCreateCustomerAccount() {
        RegisterRequest request = new RegisterRequest(
                "Thuy.My",
                "MyPassword123",
                "Pham Thi Thuy My",
                "+84912345678",
                "THUY.MY.TEST@EXAMPLE.COM"
        );

        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("$2a$10$encodedPassword");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("thuy.my", savedUser.getUsername());
        assertEquals("Pham Thi Thuy My", savedUser.getFullName());
        assertEquals("+84912345678", savedUser.getPhone());
        assertEquals(
                "thuy.my.test@example.com",
                savedUser.getEmail()
        );
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertEquals(
                AccountStatus.PENDING_ACTIVATION,
                savedUser.getAccountStatus()
        );

        assertNotEquals(
                request.password(),
                savedUser.getPasswordHash()
        );

        assertEquals(
                "$2a$10$encodedPassword",
                savedUser.getPasswordHash()
        );

        assertEquals("thuy.my", response.username());
        assertEquals(Role.CUSTOMER, response.role());
        assertEquals(
                AccountStatus.PENDING_ACTIVATION,
                response.accountStatus()
        );
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        RegisterRequest request = createValidRequest();

        when(userRepository.existsByUsernameIgnoreCase("thuy.my"))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                ErrorCode.USERNAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_WithExistingPhone_ShouldThrowException() {
        RegisterRequest request = createValidRequest();

        when(userRepository.existsByUsernameIgnoreCase("thuy.my"))
                .thenReturn(false);

        when(userRepository.existsByPhone("+84912345678"))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                ErrorCode.PHONE_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        RegisterRequest request = createValidRequest();

        when(userRepository.existsByUsernameIgnoreCase("thuy.my"))
                .thenReturn(false);

        when(userRepository.existsByPhone("+84912345678"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase(
                "thuy.my.test@example.com"
        )).thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                ErrorCode.EMAIL_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    private RegisterRequest createValidRequest() {
        return new RegisterRequest(
                "thuy.my",
                "MyPassword123",
                "Pham Thi Thuy My",
                "+84912345678",
                "thuy.my.test@example.com"
        );
    }
}