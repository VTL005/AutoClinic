package com.autoservice.billingservice.service;

import com.autoservice.billingservice.config.OnePayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnePaySignatureService {

    private static final String HMAC_ALGORITHM =
            "HmacSHA256";

    private final OnePayProperties onePayProperties;

    public String createSignature(
            Map<String, String> parameters
    ) {
        try {
            String data = buildSignatureData(parameters);

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            decodeHex(
                                    onePayProperties.getHashKey()
                            ),
                            HMAC_ALGORITHM
                    );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return encodeHex(hash);
        }
        catch (Exception exception) {
            throw new IllegalStateException(
                    "Không thể tạo chữ ký OnePay.",
                    exception
            );
        }
    }

    public boolean verifySignature(
            Map<String, String> parameters,
            String receivedSignature
    ) {
        if (receivedSignature == null
                || receivedSignature.isBlank()) {
            return false;
        }

        String expectedSignature =
                createSignature(parameters);

        return MessageDigest.isEqual(
                expectedSignature
                        .toUpperCase()
                        .getBytes(StandardCharsets.UTF_8),
                receivedSignature
                        .toUpperCase()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    private String buildSignatureData(
            Map<String, String> parameters
    ) {
        return parameters
                .entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().startsWith("vpc_")
                                || entry.getKey()
                                .startsWith("user_")
                )
                .filter(entry ->
                        !"vpc_SecureHash".equals(
                                entry.getKey()
                        )
                )
                .filter(entry ->
                        !"vpc_SecureHashType".equals(
                                entry.getKey()
                        )
                )
                .filter(entry ->
                        entry.getValue() != null
                                && !entry.getValue().isBlank()
                )
                .sorted(
                        Comparator.comparing(
                                Map.Entry::getKey
                        )
                )
                .map(entry ->
                        entry.getKey()
                                + "="
                                + entry.getValue()
                )
                .collect(
                        Collectors.joining("&")
                );
    }

    private byte[] decodeHex(
            String hex
    ) {
        if (hex == null
                || hex.isBlank()
                || hex.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Hash Key OnePay không hợp lệ."
            );
        }

        byte[] result =
                new byte[hex.length() / 2];

        for (int index = 0;
             index < hex.length();
             index += 2) {

            int high = Character.digit(
                    hex.charAt(index),
                    16
            );

            int low = Character.digit(
                    hex.charAt(index + 1),
                    16
            );

            if (high < 0 || low < 0) {
                throw new IllegalArgumentException(
                        "Hash Key OnePay phải ở dạng hexadecimal."
                );
            }

            result[index / 2] =
                    (byte) ((high << 4) + low);
        }

        return result;
    }

    private String encodeHex(
            byte[] bytes
    ) {
        StringBuilder result =
                new StringBuilder(
                        bytes.length * 2
                );

        for (byte value : bytes) {
            result.append(
                    String.format(
                            "%02X",
                            value & 0xFF
                    )
            );
        }

        return result.toString();
    }
}