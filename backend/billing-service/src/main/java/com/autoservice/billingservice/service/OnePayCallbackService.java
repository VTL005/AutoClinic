package com.autoservice.billingservice.service;

import com.autoservice.billingservice.domain.enums.PaymentStatus;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.exception.InvalidInvoiceStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnePayCallbackService {

    private final OnePaySignatureService signatureService;
    private final PaymentService paymentService;

    @Transactional
    public PaymentResponse processCallback(
            Map<String, String> parameters
    ) {
        String receivedSignature =
                parameters.get("vpc_SecureHash");

        if (!signatureService.verifySignature(
                parameters,
                receivedSignature
        )) {
            throw new InvalidInvoiceStateException(
                    "Chữ ký phản hồi OnePay không hợp lệ."
            );
        }

        String providerOrderCode =
                parameters.get("vpc_MerchTxnRef");

        if (providerOrderCode == null
                || providerOrderCode.isBlank()) {
            throw new InvalidInvoiceStateException(
                    "OnePay không trả về mã giao dịch."
            );
        }

        PaymentResponse payment =
                paymentService
                        .getPaymentByProviderOrderCode(
                                providerOrderCode
                        );

        validateAmount(
                payment,
                parameters.get("vpc_Amount")
        );

        /*
         * Callback có thể được OnePay gửi lại nhiều lần.
         * Nếu giao dịch đã hoàn thành thì chỉ trả kết quả hiện tại.
         */
        if (payment.status()
                == PaymentStatus.COMPLETED) {
            return payment;
        }

        String responseCode =
                parameters.get("vpc_TxnResponseCode");

        String transactionReference =
                parameters.get("vpc_TransactionNo");

        if ("0".equals(responseCode)) {
            paymentService.completeOnlinePayment(
                    providerOrderCode,
                    transactionReference
            );

            return paymentService
                    .getPaymentByProviderOrderCode(
                            providerOrderCode
                    );
        }

        PaymentStatus failureStatus =
                "99".equals(responseCode)
                        ? PaymentStatus.CANCELLED
                        : PaymentStatus.FAILED;

        String failureReason =
                "OnePay từ chối giao dịch. Mã phản hồi: "
                        + (
                        responseCode == null
                                ? "UNKNOWN"
                                : responseCode
                );

        return paymentService.failOnlinePayment(
                providerOrderCode,
                failureStatus,
                failureReason
        );
    }

    private void validateAmount(
            PaymentResponse payment,
            String returnedAmount
    ) {
        if (returnedAmount == null
                || returnedAmount.isBlank()) {
            throw new InvalidInvoiceStateException(
                    "OnePay không trả về số tiền giao dịch."
            );
        }

        try {
            BigDecimal amountFromOnePay =
                    new BigDecimal(returnedAmount)
                            .movePointLeft(2);

            if (payment.amount().compareTo(
                    amountFromOnePay
            ) != 0) {
                throw new InvalidInvoiceStateException(
                        "Số tiền OnePay trả về không khớp."
                );
            }
        }
        catch (NumberFormatException exception) {
            throw new InvalidInvoiceStateException(
                    "Số tiền OnePay trả về không hợp lệ."
            );
        }
    }
}