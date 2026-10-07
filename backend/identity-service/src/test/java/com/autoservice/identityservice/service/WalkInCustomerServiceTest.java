package com.autoservice.identityservice.service;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.CreateWalkInCustomerRequest;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class WalkInCustomerServiceTest {
 @Mock UserRepository users; @Mock PasswordEncoder encoder;
 @InjectMocks WalkInCustomerService service;
 @Test void existingCustomerReusedWithoutOverwriting(){
  User user=User.builder().id(5L).role(Role.CUSTOMER).fullName("Khách cũ").phone("0912345678").build();
  when(users.findByCanonicalPhoneAndDeletedFalse("+84912345678")).thenReturn(Optional.of(user));
  var result=service.create(new CreateWalkInCustomerRequest("Tên nhập lại","+84 912345678",null));
  assertThat(result.id()).isEqualTo(5L);assertThat(result.created()).isFalse();assertThat(result.fullName()).isEqualTo("Khách cũ");
  verify(users,never()).saveAndFlush(any());verifyNoInteractions(encoder);
 }
 @Test void newCustomerHasNoUsableLogin(){
  when(users.findByCanonicalPhoneAndDeletedFalse(anyString())).thenReturn(Optional.empty());
  when(encoder.encode(anyString())).thenReturn("random-encoded-password");
  when(users.saveAndFlush(any(User.class))).thenAnswer(i->i.getArgument(0));
  var result=service.create(new CreateWalkInCustomerRequest("Khách mới","0912345678",null));
  var captor=ArgumentCaptor.forClass(User.class);verify(users).saveAndFlush(captor.capture());
  assertThat(result.guest()).isTrue();assertThat(captor.getValue().getPhone()).isEqualTo("+84912345678");
  assertThat(captor.getValue().getAccountStatus().name()).isEqualTo("PENDING_ACTIVATION");
 }
}
