package com.autoservice.identityservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "account_activation_codes")
@Getter @Setter @NoArgsConstructor
public class AccountActivationCode {
    @Id @Column(name = "user_id")
    private Long userId;
    @Column(name = "code_hash", length = 255)
    private String codeHash;
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;
    @Column(name = "expires_at")
    private Instant expiresAt;
    @Column(name = "consumed_at")
    private Instant consumedAt;
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;
    @Column(name = "last_issued_at")
    private Instant lastIssuedAt;
    @Column(name = "send_window_start")
    private Instant sendWindowStart;
    @Column(name = "send_count", nullable = false)
    private int sendCount;
    @Version @Column(nullable = false)
    private Long version;
}
