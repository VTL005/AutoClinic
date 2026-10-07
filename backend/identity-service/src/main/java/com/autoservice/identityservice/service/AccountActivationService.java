package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.AccountActivationCode;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.ConfirmActivationCodeRequest;
import com.autoservice.identityservice.dto.request.RequestActivationCodeRequest;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.repository.AccountActivationCodeRepository;
import com.autoservice.identityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
public class AccountActivationService {
    private final UserRepository users;
    private final AccountActivationCodeRepository codes;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public AccountActivationService(UserRepository users, AccountActivationCodeRepository codes,
            PasswordEncoder encoder, ApplicationEventPublisher events) {
        this(users, codes, encoder, events, Clock.systemUTC());
    }

    AccountActivationService(UserRepository users, AccountActivationCodeRepository codes,
            PasswordEncoder encoder, ApplicationEventPublisher events, Clock clock) {
        this.users = users; this.codes = codes; this.encoder = encoder;
        this.events = events; this.clock = clock;
    }

    // AuthService calls this after inserting the user, in the registration transaction.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void issueForNewUser(User user) {
        if (!canActivate(user)) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT_STATUS,
                    "Chỉ khách hàng đang chờ kích hoạt được nhận mã.");
        }
        issue(user);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void requestCode(RequestActivationCodeRequest request) {
        User found = users.findByUsernameIgnoreCaseAndDeletedFalse(normalize(request.username()))
                .orElse(null);
        if (found == null) return;
        User user = users.lockActiveRecord(found.getId()).orElse(null);
        if (user == null || !canActivate(user)) return;
        issue(user);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void confirm(ConfirmActivationCodeRequest request) {
        User found = users.findByUsernameIgnoreCaseAndDeletedFalse(normalize(request.username()))
                .orElseThrow(this::invalidCode);
        User user = users.lockActiveRecord(found.getId()).orElseThrow(this::invalidCode);
        if (!canActivate(user)) throw invalidCode();
        AccountActivationCode code = codes.lockCode(user.getId()).orElseThrow(this::invalidCode);
        Instant now = clock.instant();
        if (code.getConsumedAt() != null || code.getCodeHash() == null
                || code.getExpiresAt() == null || !code.getExpiresAt().isAfter(now)
                || !code.getRecipientEmail().equalsIgnoreCase(user.getEmail().trim())) {
            throw invalidCode();
        }
        if (code.getFailedAttempts() >= 5) {
            throw new BusinessException(ErrorCode.ACTIVATION_ATTEMPTS_EXCEEDED,
                    "Đã nhập sai mã quá 5 lần. Vui lòng yêu cầu mã mới.");
        }
        if (!encoder.matches(request.code(), code.getCodeHash())) {
            code.setFailedAttempts(code.getFailedAttempts() + 1);
            codes.saveAndFlush(code);
            throw invalidCode();
        }
        code.setConsumedAt(now);
        code.setCodeHash(null);
        user.setEmailVerifiedAt(now);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        codes.saveAndFlush(code);
        users.saveAndFlush(user);
    }

    private void issue(User user) {
        Instant now = clock.instant();
        AccountActivationCode code = codes.lockCode(user.getId()).orElse(null);
        if (code == null) {
            code = new AccountActivationCode();
            code.setUserId(user.getId());
        }
        if (code.getLastIssuedAt() != null && now.isBefore(code.getLastIssuedAt().plusSeconds(60))) {
            throw new BusinessException(ErrorCode.ACTIVATION_RATE_LIMITED,
                    "Vui lòng đợi 60 giây trước khi yêu cầu mã mới.");
        }
        if (code.getSendWindowStart() == null || !now.isBefore(code.getSendWindowStart().plusSeconds(3600))) {
            code.setSendWindowStart(now);
            code.setSendCount(0);
        }
        if (code.getSendCount() >= 5) {
            throw new BusinessException(ErrorCode.ACTIVATION_RATE_LIMITED,
                    "Mỗi tài khoản được yêu cầu tối đa 5 mã trong một giờ.");
        }
        String digits = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        code.setCodeHash(encoder.encode(digits));
        code.setRecipientEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        code.setExpiresAt(now.plusSeconds(600));
        code.setConsumedAt(null);
        code.setFailedAttempts(0);
        code.setLastIssuedAt(now);
        code.setSendCount(code.getSendCount() + 1);
        codes.saveAndFlush(code);
        events.publishEvent(new ActivationCodeIssuedEvent(user.getId(), code.getRecipientEmail(),
                user.getFullName(), digits, code.getExpiresAt()));
    }

    private boolean canActivate(User user) {
        return !user.isGuest() && !user.isDeleted() && user.getRole() == Role.CUSTOMER
                && user.getAccountStatus() == AccountStatus.PENDING_ACTIVATION
                && user.getEmail() != null && !user.getEmail().isBlank();
    }

    private String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
    private BusinessException invalidCode() {
        return new BusinessException(ErrorCode.ACTIVATION_CODE_INVALID,
                "Mã kích hoạt không hợp lệ, đã hết hạn hoặc đã được sử dụng.");
    }
}
