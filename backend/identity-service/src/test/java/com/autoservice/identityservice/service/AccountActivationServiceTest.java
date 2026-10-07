package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.AccountActivationCode;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.ConfirmActivationCodeRequest;
import com.autoservice.identityservice.dto.request.RequestActivationCodeRequest;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.repository.AccountActivationCodeRepository;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountActivationServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AccountActivationCodeRepository codes = mock(AccountActivationCodeRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-04T05:00:00Z"), ZoneOffset.UTC);
    private final AccountActivationService service = new AccountActivationService(users, codes, encoder, events, clock);

    private User pendingUser() {
        User user = new User(); user.setId(5L); user.setUsername("customer");
        user.setRole(Role.CUSTOMER); user.setAccountStatus(AccountStatus.PENDING_ACTIVATION);
        user.setEmail("customer@example.com"); user.setFullName("Customer");
        return user;
    }

    private AccountActivationCode code() {
        AccountActivationCode code = new AccountActivationCode();
        code.setUserId(5L); code.setCodeHash("encoded");
        code.setRecipientEmail("customer@example.com");
        code.setExpiresAt(clock.instant().plusSeconds(600));
        return code;
    }

    private void lookup(User user, AccountActivationCode code) {
        when(users.findByUsernameIgnoreCaseAndDeletedFalse("customer")).thenReturn(Optional.of(user));
        when(users.lockActiveRecord(5L)).thenReturn(Optional.of(user));
        when(codes.lockCode(5L)).thenReturn(Optional.ofNullable(code));
    }

    @Test
    void successfulCodeActivatesAndConsumesOnlyOnce() {
        User user = pendingUser(); AccountActivationCode code = code(); lookup(user, code);
        when(encoder.matches("123456", "encoded")).thenReturn(true);
        service.confirm(new ConfirmActivationCodeRequest("customer", "123456"));
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt()).isEqualTo(clock.instant());
        assertThat(code.getCodeHash()).isNull();
        assertThat(code.getConsumedAt()).isEqualTo(clock.instant());
        assertThatThrownBy(() -> service.confirm(new ConfirmActivationCodeRequest("customer", "123456")))
                .isInstanceOf(BusinessException.class);
        verify(users, times(1)).saveAndFlush(user);
    }

    @Test
    void expiredCodeNeverActivates() {
        User user = pendingUser(); AccountActivationCode code = code(); lookup(user, code);
        code.setExpiresAt(clock.instant());
        assertThatThrownBy(() -> service.confirm(new ConfirmActivationCodeRequest("customer", "123456")))
                .isInstanceOf(BusinessException.class);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
        verifyNoInteractions(encoder);
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void wrongCodeIncrementsCounterAndSixthAttemptDoesNotCheckPassword() {
        User user = pendingUser(); AccountActivationCode code = code(); lookup(user, code);
        code.setFailedAttempts(4);
        assertThatThrownBy(() -> service.confirm(new ConfirmActivationCodeRequest("customer", "000000")))
                .isInstanceOf(BusinessException.class);
        assertThat(code.getFailedAttempts()).isEqualTo(5);
        verify(codes).saveAndFlush(code);
        assertThatThrownBy(() -> service.confirm(new ConfirmActivationCodeRequest("customer", "000000")))
                .isInstanceOf(BusinessException.class);
        verify(encoder, times(1)).matches("000000", "encoded");
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
    }

    @Test
    void resendCooldownPreventsEmailEvent() {
        User user = pendingUser(); AccountActivationCode code = code(); lookup(user, code);
        code.setLastIssuedAt(clock.instant().minusSeconds(59));
        assertThatThrownBy(() -> service.requestCode(new RequestActivationCodeRequest("customer")))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(events, encoder);
    }

    @Test
    void hourlyLimitPreventsSixthEmail() {
        User user = pendingUser(); AccountActivationCode code = code(); lookup(user, code);
        code.setLastIssuedAt(clock.instant().minusSeconds(60));
        code.setSendWindowStart(clock.instant().minusSeconds(500)); code.setSendCount(5);
        assertThatThrownBy(() -> service.requestCode(new RequestActivationCodeRequest("customer")))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(events, encoder);
    }

    @Test
    void reissueReplacesOldHashAndResetsFailedAttempts() {
        User user = pendingUser(); AccountActivationCode code = code(); lookup(user, code);
        code.setLastIssuedAt(clock.instant().minusSeconds(60));
        code.setSendWindowStart(clock.instant().minusSeconds(500));
        code.setSendCount(1); code.setFailedAttempts(5);
        when(encoder.encode(anyString())).thenReturn("new-hash");
        service.requestCode(new RequestActivationCodeRequest("customer"));
        assertThat(code.getCodeHash()).isEqualTo("new-hash");
        assertThat(code.getFailedAttempts()).isZero();
        assertThat(code.getSendCount()).isEqualTo(2);
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(event.capture());
        ActivationCodeIssuedEvent issued = (ActivationCodeIssuedEvent) event.getValue();
        assertThat(issued.code()).matches("[0-9]{6}");
        assertThat(issued.expiresAt()).isEqualTo(clock.instant().plusSeconds(600));
        verify(encoder).encode(issued.code());
    }

    @Test
    void disabledAccountCannotSelfActivate() {
        User user = pendingUser(); user.setAccountStatus(AccountStatus.DISABLED);
        when(users.findByUsernameIgnoreCaseAndDeletedFalse("customer")).thenReturn(Optional.of(user));
        when(users.lockActiveRecord(5L)).thenReturn(Optional.of(user));
        service.requestCode(new RequestActivationCodeRequest("customer"));
        verifyNoInteractions(codes, encoder, events);
        assertThatThrownBy(() -> service.confirm(new ConfirmActivationCodeRequest("customer", "123456")))
                .isInstanceOf(BusinessException.class);
    }
}
