package com.autoservice.vehicleservice.controller;
import com.autoservice.vehicleservice.common.ApiResponse;
import com.autoservice.vehicleservice.dto.request.CreateVehicleRequest;
import com.autoservice.vehicleservice.dto.response.VehicleResponse;
import com.autoservice.vehicleservice.service.VehicleService;
import com.autoservice.vehicleservice.service.ReceptionCustomerClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/vehicles/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WalkInVehicleController {
 private final VehicleService vehicles;
 private final ReceptionCustomerClient customers;
 @PostMapping("/{customerId}")
 public ResponseEntity<ApiResponse<VehicleResponse>> create(@PathVariable Long customerId,
   @RequestHeader("Authorization") String authorization, @Valid @RequestBody CreateVehicleRequest request) {
  customers.requireCustomer(customerId,authorization);
  return ResponseEntity.status(201).body(ApiResponse.success("Thêm xe cho khách hàng thành công.",vehicles.createVehicle(customerId,request)));
 }
}
