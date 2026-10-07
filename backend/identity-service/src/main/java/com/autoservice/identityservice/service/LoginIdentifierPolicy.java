package com.autoservice.identityservice.service;

import java.util.List;
import java.util.Locale;

public final class LoginIdentifierPolicy {
    private LoginIdentifierPolicy() {}

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static List<String> phoneCandidates(String value) {
        String input = normalize(value);
        // Do not reinterpret ordinary usernames or email addresses as phone numbers.
        if (!input.matches("[+0-9().\\s-]+")) return List.of();
        String compact = input.replaceAll("[().\\s-]", "");
        String local;
        if (compact.matches("0[0-9]{9}")) {
            local = compact;
        } else if (compact.matches("\\+?84[0-9]{9}")) {
            local = "0" + compact.replaceFirst("^\\+?84", "");
        } else {
            if (compact.matches("\\+?[1-9][0-9]{7,13}")) {
                String digits = compact.replaceFirst("^\\+", "");
                return List.of(digits, "+" + digits);
            }
            return List.of();
        }
        return List.of(local, "+84" + local.substring(1), "84" + local.substring(1));
    }
}
