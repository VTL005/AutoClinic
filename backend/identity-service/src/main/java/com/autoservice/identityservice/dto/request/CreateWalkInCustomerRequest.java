package com.autoservice.identityservice.dto.request;
import jakarta.validation.constraints.*;
public record CreateWalkInCustomerRequest(
        @NotBlank @Size(min=2,max=100) String fullName,
        @NotBlank @Pattern(regexp=com.autoservice.identityservice.service.PhoneNumbers.INPUT_PATTERN) String phone,
        @Email @Size(max=150) String email) {}
