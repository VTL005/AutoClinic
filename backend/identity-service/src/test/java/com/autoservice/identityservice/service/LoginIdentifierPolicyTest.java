package com.autoservice.identityservice.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class LoginIdentifierPolicyTest {
    @Test
    void trimsAndNormalizesEmailAndUsernameCase() {
        assertThat(LoginIdentifierPolicy.normalize("  Customer@Example.COM  "))
                .isEqualTo("customer@example.com");
        assertThat(LoginIdentifierPolicy.normalize("  Mechanic.An  ")).isEqualTo("mechanic.an");
    }

    @Test
    void localAndInternationalNumbersHaveTheSameCandidates() {
        var expected = LoginIdentifierPolicy.phoneCandidates("0912345678");
        assertThat(expected).containsExactly("0912345678", "+84912345678", "84912345678");
        assertThat(LoginIdentifierPolicy.phoneCandidates("+84 912-345-678")).isEqualTo(expected);
        assertThat(LoginIdentifierPolicy.phoneCandidates("84.912.345.678")).isEqualTo(expected);
        assertThat(LoginIdentifierPolicy.phoneCandidates("(0912) 345 678")).isEqualTo(expected);
    }

    @Test
    void doesNotTreatUsernamesOrInvalidNumbersAsPhoneAliases() {
        assertThat(LoginIdentifierPolicy.phoneCandidates("mechanic.an")).isEmpty();
        assertThat(LoginIdentifierPolicy.phoneCandidates("customer@example.com")).isEmpty();
        assertThat(LoginIdentifierPolicy.phoneCandidates("09123")).isEmpty();
        assertThat(LoginIdentifierPolicy.phoneCandidates("+1 9123456789")).containsExactly("19123456789", "+19123456789");
    }
}
