package com.autoservice.billingservice.service;

import com.autoservice.billingservice.domain.enums.PaymentMethod;
import com.autoservice.billingservice.domain.enums.PaymentStatus;
import com.autoservice.billingservice.dto.request.InitiateOnlinePaymentRequest;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.exception.InvalidInvoiceStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Service
@RequiredArgsConstructor
public class PayOSPaymentService {

    private static final long ORDER_CODE_PREFIX =
            1_000_000_000_000L;

    private final PayOS payOS;

    private final PaymentService paymentService;

    @Value("${app.payment.payos.return-url}")
    private String returnUrl;

    @Value("${app.payment.payos.cancel-url}")
    private String cancelUrl;

    public PaymentResponse createPaymentLink(
            Long invoiceId,
            Long customerUserId,
            InitiateOnlinePaymentRequest request
    ) {
        validatePayOSMethod(
                request.paymentMethod()
        );

        long amount =
                convertAmountToLong(
                        request.amount()
                );

        PaymentResponse payment =
                paymentService
                        .createOnlinePaymentAttempt(
                                invoiceId,
                                customerUserId,
                                request
                        );

        long orderCode =
                generateOrderCode(
                        payment.id()
                );

        String providerOrderCode =
                String.valueOf(orderCode);

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusMinutes(15);

        paymentService.updateGatewayDetails(
                payment.id(),
                providerOrderCode,
                null,
                null,
                expiresAt
        );

        try {
            CreatePaymentLinkRequest paymentRequest =
                    CreatePaymentLinkRequest
                            .builder()
                            .orderCode(orderCode)
                            .amount(amount)
                            .description(
                                    createDescription(
                                            invoiceId
                                    )
                            )
                            .returnUrl(returnUrl)
                            .cancelUrl(cancelUrl)
                            .expiredAt(
                                    expiresAt
                                            .atZone(
                                                    ZoneId.of(
                                                            "Asia/Ho_Chi_Minh"
                                                    )
                                            )
                                            .toEpochSecond()
                            )
                            .build();

            CreatePaymentLinkResponse paymentLink =
                    payOS.paymentRequests()
                            .create(paymentRequest);

            return paymentService
                    .updateGatewayDetails(
                            payment.id(),
                            providerOrderCode,
                            paymentLink
                                    .getCheckoutUrl(),
                            paymentLink
                                    .getQrCode(),
                            expiresAt
                    );
        }
        catch (Exception exception) {
            markPaymentAsFailed(
                    providerOrderCode
            );

            throw new InvalidInvoiceStateException(
                    "Không thể tạo liên kết thanh toán "
                            + "payOS. Vui lòng thử lại."
            );
        }
    }

    private void validatePayOSMethod(
            PaymentMethod paymentMethod
    ) {
        if (paymentMethod
                != PaymentMethod.PAYOS) {
            throw new InvalidInvoiceStateException(
                    "API này chỉ hỗ trợ thanh toán "
                            + "bằng payOS."
            );
        }
    }

    private long convertAmountToLong(
            BigDecimal amount
    ) {
        try {
            long convertedAmount =
                    amount.longValueExact();

            if (convertedAmount <= 0) {
                throw new ArithmeticException();
            }

            return convertedAmount;
        }
        catch (ArithmeticException exception) {
            throw new InvalidInvoiceStateException(
                    "Số tiền thanh toán payOS phải là "
                            + "số nguyên dương theo đơn vị VND."
            );
        }
    }

    private long generateOrderCode(
            Long paymentId
    ) {
        return ORDER_CODE_PREFIX
                + paymentId;
    }

    private String createDescription(
            Long invoiceId
    ) {
        return "Hoa don " + invoiceId;
    }

    private void markPaymentAsFailed(
            String providerOrderCode
    ) {
        try {
            paymentService.failOnlinePayment(
                    providerOrderCode,
                    PaymentStatus.FAILED,
                    "Không thể tạo liên kết "
                            + "thanh toán payOS."
            );
        }
        catch (Exception ignored) {
            // Không che mất lỗi gốc từ payOS.
        }
    }
}