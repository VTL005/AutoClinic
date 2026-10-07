package com.autoservice.identityservice.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class ActivationEmailService {
    private static final Logger log = LoggerFactory.getLogger(ActivationEmailService.class);
    private final JavaMailSender sender;
    private final String from;

    public ActivationEmailService(@Qualifier("activationMailSender") JavaMailSender sender,
            @Value("${app.activation.mail.username:${spring.mail.username:${MAIL_USERNAME:}}}") String from) {
        this.sender = sender; this.from = from;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(ActivationCodeIssuedEvent event) {
        try {
            if (from == null || from.isBlank()) {
                log.warn("Activation email not configured; user {} remains pending", event.userId());
                return;
            }
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from.trim(), "AutoService");
            helper.setTo(event.email());
            helper.setSubject("[AutoService] Mã kích hoạt tài khoản");
            String plain = "Kính chào " + event.fullName() + ",\n\nMã kích hoạt tài khoản AutoService: "
                    + event.code() + "\nMã có hiệu lực 10 phút và chỉ sử dụng một lần."
                    + "\nKhông chia sẻ mã này với người khác.\n\nTrân trọng,\nAutoService";
            String html = """
                <!DOCTYPE html><html lang="vi"><body style="margin:0;padding:24px;background:#f1efeb;">
                <table role="presentation" style="width:100%%;max-width:580px;margin:auto;border-collapse:collapse;background:#fff;">
                <tr><td style="padding:28px;background:#242420;color:#d8c6a3;font-family:Arial,sans-serif;font-size:24px;">AutoService</td></tr>
                <tr><td style="padding:28px;font-family:Arial,sans-serif;color:#333;line-height:1.7;">
                <h2 style="margin:0 0 16px;font-size:22px;">Kích hoạt tài khoản</h2>
                <p>Kính chào %s,</p><p>Vui lòng nhập mã dưới đây để kích hoạt tài khoản AutoService:</p>
                <div style="padding:18px;background:#f6f3ed;text-align:center;font-size:30px;letter-spacing:8px;color:#625337;">%s</div>
                <p>Mã có hiệu lực <strong>10 phút</strong> và chỉ sử dụng một lần.</p>
                <p>Không chia sẻ mã với người khác. Nếu không đăng ký tài khoản, Quý khách có thể bỏ qua email này.</p>
                <p>Trân trọng,<br>AutoService</p></td></tr></table></body></html>
                """.formatted(escape(event.fullName()), event.code());
            helper.setText(plain, html);
            sender.send(message);
        } catch (Exception exception) {
            // Sending failure must not roll back the already committed registration.
            log.warn("Activation email failed for user {}; customer can request a new code. Error type: {}",
                    event.userId(), exception.getClass().getSimpleName());
        }
    }

    private String escape(String value) {
        if (value == null) return "Quý khách";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
