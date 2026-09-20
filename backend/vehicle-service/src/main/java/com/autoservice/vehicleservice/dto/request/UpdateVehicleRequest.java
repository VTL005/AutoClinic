package com.autoservice.vehicleservice.dto.request;

import com.autoservice.vehicleservice.domain.enums.FuelType;
import com.autoservice.vehicleservice.domain.enums.TransmissionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(

        @Size(
                max = 20,
                message = "Biển số xe không được vượt quá 20 ký tự"
        )
        @Pattern(
                regexp = "(?i)^$|^[A-Z0-9.\\-\\s]{5,20}$",
                message = "Biển số xe không đúng định dạng"
        )
        String licensePlate,

        @NotBlank(message = "Hãng xe không được để trống")
        @Size(
                max = 100,
                message = "Hãng xe không được vượt quá 100 ký tự"
        )
        String manufacturer,

        @NotBlank(message = "Mẫu xe không được để trống")
        @Size(
                max = 100,
                message = "Mẫu xe không được vượt quá 100 ký tự"
        )
        String model,

        @NotNull(message = "Năm sản xuất không được để trống")
        @Min(
                value = 1886,
                message = "Năm sản xuất không được nhỏ hơn 1886"
        )
        @Max(
                value = 2100,
                message = "Năm sản xuất không được lớn hơn 2100"
        )
        Integer manufactureYear,

        @Size(
                max = 50,
                message = "Màu xe không được vượt quá 50 ký tự"
        )
        String color,

        @Size(
                max = 100,
                message = "Loại động cơ không được vượt quá 100 ký tự"
        )
        String engineType,

        @NotNull(message = "Loại nhiên liệu không được để trống")
        FuelType fuelType,

        TransmissionType transmissionType,

        @NotNull(message = "Số kilomet không được để trống")
        @PositiveOrZero(
                message = "Số kilomet không được là số âm"
        )
        @Max(
                value = 4_294_967_295L,
                message = "Số kilomet vượt quá giới hạn cho phép"
        )
        Long odometerKm
) {
}