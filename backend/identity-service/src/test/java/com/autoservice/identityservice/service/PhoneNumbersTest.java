package com.autoservice.identityservice.service;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class PhoneNumbersTest {
 @Test void aliasesShareOneCanonicalPhone(){
  for(String value:new String[]{"0912345678","+84912345678","84912345678","(0912) 345-678"})
   assertThat(PhoneNumbers.normalize(value)).isEqualTo("+84912345678");
 }
 @Test void internationalPhonesRemainDistinct(){assertThat(PhoneNumbers.normalize("+1 9123456789")).isEqualTo("+19123456789");}
 @Test void invalidPhonesRejected(){for(String value:new String[]{"09123","phone","++84912345678","0000000000000000"})assertThatThrownBy(()->PhoneNumbers.normalize(value)).isInstanceOf(com.autoservice.identityservice.exception.BusinessException.class);}
}
