package com.autoservice.identityservice.service;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.ErrorCode;
public final class PhoneNumbers {
    private PhoneNumbers() {}
    public static final String INPUT_PATTERN = "[+0-9().\\s-]{8,30}";
    public static String normalize(String value) {
        if (value == null || !value.trim().matches(INPUT_PATTERN)) throw invalid();
        String digits = value.trim().replaceAll("[().\\s-]", "");
        if (digits.matches("0[0-9]{9}")) return "+84" + digits.substring(1);
        if (digits.matches("\\+?84[0-9]{9}")) return "+" + digits.replaceFirst("^\\+", "");
        if (digits.matches("\\+?[1-9][0-9]{7,13}")) return "+" + digits.replaceFirst("^\\+", "");
        throw invalid();
    }
    private static BusinessException invalid() {
        return new BusinessException(ErrorCode.VALIDATION_ERROR, "Số điện thoại không đúng định dạng.");
    }
}
