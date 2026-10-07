package com.autoservice.identityservice.service;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.dto.request.CreateWalkInCustomerRequest;
import com.autoservice.identityservice.repository.UserRepository;
import com.autoservice.identityservice.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.Locale;
@Service @RequiredArgsConstructor
public class WalkInCustomerService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public record Customer(Long id,String fullName,String phone,String email,boolean guest,boolean created) {}
    @Transactional
    public Customer create(CreateWalkInCustomerRequest request) {
        String phone = PhoneNumbers.normalize(request.phone());
        User existing = users.findByCanonicalPhoneAndDeletedFalse(phone).orElse(null);
        if (existing != null) {
            if (existing.getRole()!=Role.CUSTOMER) throw new BusinessException(
                    ErrorCode.INVALID_ACCOUNT_STATUS,"Số điện thoại thuộc tài khoản nhân viên.");
            return result(existing,false);
        }
        if (users.existsByCanonicalPhone(phone)) throw new DuplicateResourceException(
                ErrorCode.PHONE_ALREADY_EXISTS,"Số điện thoại thuộc hồ sơ đã xóa; cần admin kiểm tra.");
        String email = request.email()==null||request.email().isBlank()?null:request.email().trim().toLowerCase(Locale.ROOT);
        if(email!=null && users.existsByEmailIgnoreCase(email)) throw new DuplicateResourceException(
                ErrorCode.EMAIL_ALREADY_EXISTS,"Email đã được sử dụng.");
        User user = new User();
        user.setUsername("guest."+UUID.randomUUID().toString().replace("-",""));
        user.setPasswordHash(encoder.encode(UUID.randomUUID().toString()));
        user.setFullName(request.fullName().trim());user.setPhone(phone);user.setEmail(email);
        user.setRole(Role.CUSTOMER);user.setGuest(true);
        user.setAccountStatus(AccountStatus.PENDING_ACTIVATION);
        return result(users.saveAndFlush(user),true);
    }
    private Customer result(User user,boolean created) {
        return new Customer(user.getId(),user.getFullName(),user.getPhone(),user.getEmail(),user.isGuest(),created);
    }
}
