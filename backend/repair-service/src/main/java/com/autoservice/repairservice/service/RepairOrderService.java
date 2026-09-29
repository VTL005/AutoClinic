package com.autoservice.repairservice.service;

import com.autoservice.repairservice.client.BookingClient;
import com.autoservice.repairservice.client.dto.BookingDetailsResponse;
import com.autoservice.repairservice.domain.entity.RepairOrder;
import com.autoservice.repairservice.domain.entity.RepairStatusHistory;
import com.autoservice.repairservice.domain.enums.RepairOrderStatus;
import com.autoservice.repairservice.dto.request.CreateRepairOrderRequest;
import com.autoservice.repairservice.dto.response.RepairOrderResponse;
import com.autoservice.repairservice.exception.InvalidRepairStateException;
import com.autoservice.repairservice.mapper.RepairOrderMapper;
import com.autoservice.repairservice.repository.RepairOrderRepository;
import com.autoservice.repairservice.repository.RepairStatusHistoryRepository;
import com.autoservice.repairservice.repository.RepairTaskRepository;
import com.autoservice.repairservice.exception.ResourceNotFoundException;
import com.autoservice.repairservice.dto.request.UpdateRepairOrderDetailsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autoservice.repairservice.domain.entity.RepairTask;
import com.autoservice.repairservice.domain.enums.RepairTaskStatus;
import com.autoservice.repairservice.dto.request.AddRepairTaskRequest;
import com.autoservice.repairservice.exception.ResourceNotFoundException;
import com.autoservice.repairservice.dto.request.UpdateRepairOrderStatusRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.autoservice.repairservice.dto.request.UpdateRepairTaskStatusRequest;
@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderRepository
            repairOrderRepository;

    private final RepairTaskRepository
            repairTaskRepository;

    private final RepairStatusHistoryRepository
            repairStatusHistoryRepository;

    private final BookingClient bookingClient;

    private final RepairOrderMapper repairOrderMapper;

    @Transactional
    public RepairOrderResponse createRepairOrder(
            Long createdByUserId,
            String authorizationHeader,
            CreateRepairOrderRequest request
    ) {
        if (repairOrderRepository.existsByBookingId(
                request.bookingId()
        )) {
            throw new InvalidRepairStateException(
                    "Lịch hẹn này đã có phiếu sửa chữa."
            );
        }

        BookingDetailsResponse booking =
                bookingClient.getBooking(
                        request.bookingId(),
                        authorizationHeader
                );

        validateBookingForRepair(booking);

        RepairOrder repairOrder =
                RepairOrder.builder()
                        .repairCode(generateRepairCode())
                        .bookingId(booking.id())
                        .customerUserId(
                                booking.customerUserId()
                        )
                        .vehicleId(booking.vehicleId())
                        .mechanicUserId(
                                booking.mechanicUserId()
                        )
                        .createdByUserId(createdByUserId)
                        .serviceType(
                                booking.serviceType().trim()
                        )
                        .customerComplaint(
                                normalizeNullableText(
                                        booking.customerNote()
                                )
                        )
                        .technicianNote(
                                normalizeNullableText(
                                        request.technicianNote()
                                )
                        )
                        .odometer(request.odometer())
                        .status(RepairOrderStatus.RECEIVED)
                        .receivedAt(LocalDateTime.now())
                        .estimatedCompletionAt(
                                request.estimatedCompletionAt()
                        )
                        .build();

        RepairOrder savedRepairOrder =
                repairOrderRepository.save(repairOrder);

        RepairStatusHistory initialHistory =
                RepairStatusHistory.builder()
                        .repairOrder(savedRepairOrder)
                        .previousStatus(null)
                        .newStatus(
                                RepairOrderStatus.RECEIVED
                        )
                        .changedByUserId(createdByUserId)
                        .note(
                                "Tạo phiếu sửa chữa từ lịch hẹn "
                                        + booking.bookingCode()
                        )
                        .build();

        RepairStatusHistory savedHistory =
                repairStatusHistoryRepository.save(
                        initialHistory
                );

        return repairOrderMapper.toResponse(
                savedRepairOrder,
                List.of(),
                List.of(savedHistory)
        );
    }
    @Transactional(readOnly = true)
    public Page<RepairOrderResponse> getRepairOrders(
            RepairOrderStatus status,
            Pageable pageable
    ) {
        Page<RepairOrder> repairOrders;

        if (status == null) {
            repairOrders =
                    repairOrderRepository.findAll(pageable);
        } else {
            repairOrders =
                    repairOrderRepository.findByStatus(
                            status,
                            pageable
                    );
        }

        return repairOrders.map(repairOrder ->
                repairOrderMapper.toResponse(
                        repairOrder,
                        repairTaskRepository
                                .findByRepairOrder_IdOrderByCreatedAtAsc(
                                        repairOrder.getId()
                                ),
                        repairStatusHistoryRepository
                                .findByRepairOrder_IdOrderByChangedAtAsc(
                                        repairOrder.getId()
                                )
                )
        );
    }
    @Transactional(readOnly = true)
    public Page<RepairOrderResponse>
    getMechanicRepairOrders(
            Long mechanicUserId,
            Pageable pageable
    ) {
        Page<RepairOrder> repairOrders =
                repairOrderRepository
                        .findByMechanicUserId(
                                mechanicUserId,
                                pageable
                        );

        return repairOrders.map(repairOrder ->
                repairOrderMapper.toResponse(
                        repairOrder,
                        repairTaskRepository
                                .findByRepairOrder_IdOrderByCreatedAtAsc(
                                        repairOrder.getId()
                                ),
                        repairStatusHistoryRepository
                                .findByRepairOrder_IdOrderByChangedAtAsc(
                                        repairOrder.getId()
                                )
                )
        );
    }
    @Transactional(readOnly = true)
    public RepairOrderResponse
    getMechanicRepairOrder(
            Long repairOrderId,
            Long mechanicUserId
    ) {
        RepairOrder repairOrder =
                repairOrderRepository
                        .findByIdAndMechanicUserId(
                                repairOrderId,
                                mechanicUserId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy phiếu sửa chữa "
                                                + "được phân công."
                                )
                        );

        return repairOrderMapper.toResponse(
                repairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }
    @Transactional
    public RepairOrderResponse
    updateMechanicRepairOrderDetails(
            Long repairOrderId,
            Long mechanicUserId,
            UpdateRepairOrderDetailsRequest request
    ) {
        repairOrderRepository
                .findByIdAndMechanicUserId(
                        repairOrderId,
                        mechanicUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy phiếu sửa chữa "
                                        + "được phân công."
                        )
                );

        return updateRepairOrderDetails(
                repairOrderId,
                request
        );
    }
    @Transactional
    public RepairOrderResponse
    updateMechanicRepairTaskStatus(
            Long repairOrderId,
            Long taskId,
            Long mechanicUserId,
            UpdateRepairTaskStatusRequest request
    ) {
        repairOrderRepository
                .findByIdAndMechanicUserId(
                        repairOrderId,
                        mechanicUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy phiếu sửa chữa "
                                        + "được phân công."
                        )
                );

        return updateRepairTaskStatus(
                repairOrderId,
                taskId,
                request
        );
    }
    @Transactional
    public RepairOrderResponse
    updateMechanicRepairOrderStatus(
            Long repairOrderId,
            Long mechanicUserId,
            UpdateRepairOrderStatusRequest request
    ) {
        repairOrderRepository
                .findByIdAndMechanicUserId(
                        repairOrderId,
                        mechanicUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy phiếu sửa chữa "
                                        + "được phân công."
                        )
                );

        validateMechanicTargetStatus(
                request.status()
        );

        return updateRepairOrderStatus(
                repairOrderId,
                mechanicUserId,
                request
        );
    }
    private void validateMechanicTargetStatus(
            RepairOrderStatus status
    ) {
        if (status == RepairOrderStatus.DELIVERED
                || status == RepairOrderStatus.CANCELLED) {
            throw new InvalidRepairStateException(
                    "Kỹ thuật viên không được chuyển phiếu "
                            + "sang trạng thái "
                            + status
                            + "."
            );
        }
    }
    @Transactional(readOnly = true)
    public Page<RepairOrderResponse>
    getCustomerRepairOrders(
            Long customerUserId,
            Pageable pageable
    ) {
        Page<RepairOrder> repairOrders =
                repairOrderRepository
                        .findByCustomerUserId(
                                customerUserId,
                                pageable
                        );

        return repairOrders.map(repairOrder ->
                repairOrderMapper.toResponse(
                        repairOrder,
                        repairTaskRepository
                                .findByRepairOrder_IdOrderByCreatedAtAsc(
                                        repairOrder.getId()
                                ),
                        repairStatusHistoryRepository
                                .findByRepairOrder_IdOrderByChangedAtAsc(
                                        repairOrder.getId()
                                )
                )
        );
    }
    @Transactional(readOnly = true)
    public RepairOrderResponse
    getCustomerRepairOrder(
            Long repairOrderId,
            Long customerUserId
    ) {
        RepairOrder repairOrder =
                repairOrderRepository
                        .findByIdAndCustomerUserId(
                                repairOrderId,
                                customerUserId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy phiếu sửa chữa "
                                                + "của khách hàng."
                                )
                        );

        return repairOrderMapper.toResponse(
                repairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }
    @Transactional(readOnly = true)
    public RepairOrderResponse getRepairOrder(
            Long repairOrderId
    ) {
        RepairOrder repairOrder =
                repairOrderRepository.findById(
                                repairOrderId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy "
                                                + "phiếu sửa chữa."
                                )
                        );

        return repairOrderMapper.toResponse(
                repairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }
    @Transactional
    public RepairOrderResponse updateRepairOrderDetails(
            Long repairOrderId,
            UpdateRepairOrderDetailsRequest request
    ) {
        RepairOrder repairOrder =
                repairOrderRepository
                        .findById(repairOrderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy phiếu sửa chữa."
                                )
                        );

        validateCanUpdateDetails(repairOrder);

        if (request.diagnosis() != null) {
            repairOrder.setDiagnosis(
                    request.diagnosis()
            );
        }

        if (request.technicianNote() != null) {
            repairOrder.setTechnicianNote(
                    request.technicianNote()
            );
        }

        if (request.odometer() != null) {
            repairOrder.setOdometer(
                    request.odometer()
            );
        }

        if (request.estimatedCompletionAt() != null) {
            repairOrder.setEstimatedCompletionAt(
                    request.estimatedCompletionAt()
            );
        }

        RepairOrder savedRepairOrder =
                repairOrderRepository.save(repairOrder);

        return repairOrderMapper.toResponse(
                savedRepairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }
    private void validateCanUpdateDetails(
            RepairOrder repairOrder
    ) {
        RepairOrderStatus status =
                repairOrder.getStatus();

        if (status == RepairOrderStatus.COMPLETED
                || status == RepairOrderStatus.DELIVERED
                || status == RepairOrderStatus.CANCELLED) {
            throw new InvalidRepairStateException(
                    "Không thể cập nhật thông tin của phiếu "
                            + "sửa chữa ở trạng thái "
                            + status
                            + "."
            );
        }
    }
    @Transactional
    public RepairOrderResponse addRepairTask(
            Long repairOrderId,
            AddRepairTaskRequest request
    ) {
        RepairOrder repairOrder =
                repairOrderRepository.findById(repairOrderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy phiếu sửa chữa."
                                )
                        );

        validateCanModifyTasks(repairOrder);

        RepairTask repairTask =
                RepairTask.builder()
                        .repairOrder(repairOrder)
                        .taskName(request.taskName().trim())
                        .description(
                                normalizeNullableText(
                                        request.description()
                                )
                        )
                        .status(RepairTaskStatus.PENDING)
                        .laborHours(request.laborHours())
                        .laborCost(
                                request.laborCost() == null
                                        ? BigDecimal.ZERO
                                        : request.laborCost()
                        )
                        .build();

        repairTaskRepository.save(repairTask);

        return repairOrderMapper.toResponse(
                repairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }
    @Transactional
    public RepairOrderResponse updateRepairTaskStatus(
            Long repairOrderId,
            Long taskId,
            UpdateRepairTaskStatusRequest request
    ) {
        RepairOrder repairOrder =
                repairOrderRepository
                        .findById(repairOrderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy phiếu sửa chữa."
                                )
                        );

        validateCanModifyTasks(repairOrder);

        RepairTask repairTask =
                repairTaskRepository
                        .findByIdAndRepairOrder_Id(
                                taskId,
                                repairOrderId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy công việc "
                                                + "sửa chữa."
                                )
                        );

        RepairTaskStatus currentStatus =
                repairTask.getStatus();

        RepairTaskStatus newStatus =
                request.status();

        if (!isValidTaskStatusTransition(
                currentStatus,
                newStatus
        )) {
            throw new InvalidRepairStateException(
                    "Không thể chuyển trạng thái công việc từ "
                            + currentStatus
                            + " sang "
                            + newStatus
                            + "."
            );
        }

        repairTask.setStatus(newStatus);

        repairTaskRepository.save(repairTask);

        return repairOrderMapper.toResponse(
                repairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }
    @Transactional

    private boolean isValidTaskStatusTransition(
            RepairTaskStatus currentStatus,
            RepairTaskStatus newStatus
    ) {
        return switch (currentStatus) {
            case PENDING ->
                    newStatus
                            == RepairTaskStatus.IN_PROGRESS
                            || newStatus
                            == RepairTaskStatus.CANCELLED;

            case IN_PROGRESS ->
                    newStatus
                            == RepairTaskStatus.COMPLETED
                            || newStatus
                            == RepairTaskStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };
    }
    private void validateCanModifyTasks(
            RepairOrder repairOrder
    ) {
        RepairOrderStatus status = repairOrder.getStatus();

        if (status == RepairOrderStatus.COMPLETED
                || status == RepairOrderStatus.DELIVERED
                || status == RepairOrderStatus.CANCELLED) {
            throw new InvalidRepairStateException(
                    "Không thể thêm công việc vào phiếu "
                            + "sửa chữa đã kết thúc."
            );
        }
    }
    @Transactional
    public RepairOrderResponse updateRepairOrderStatus(
            Long repairOrderId,
            Long changedByUserId,
            UpdateRepairOrderStatusRequest request
    ) {
        RepairOrder repairOrder =
                repairOrderRepository.findById(repairOrderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy phiếu sửa chữa."
                                )
                        );

        RepairOrderStatus previousStatus =
                repairOrder.getStatus();

        RepairOrderStatus newStatus =
                request.status();

        if (!isValidStatusTransition(
                previousStatus,
                newStatus
        )) {
            throw new InvalidRepairStateException(
                    "Không thể chuyển trạng thái từ "
                            + previousStatus
                            + " sang "
                            + newStatus
                            + "."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        repairOrder.setStatus(newStatus);

        if (newStatus == RepairOrderStatus.IN_PROGRESS
                && repairOrder.getStartedAt() == null) {
            repairOrder.setStartedAt(now);
        }

        if (newStatus == RepairOrderStatus.COMPLETED) {
            validateAllTasksFinished(repairOrderId);
            repairOrder.setCompletedAt(now);
        }

        if (newStatus == RepairOrderStatus.DELIVERED) {
            repairOrder.setDeliveredAt(now);
        }

        RepairOrder savedRepairOrder =
                repairOrderRepository.save(repairOrder);

        RepairStatusHistory history =
                RepairStatusHistory.builder()
                        .repairOrder(savedRepairOrder)
                        .previousStatus(previousStatus)
                        .newStatus(newStatus)
                        .changedByUserId(changedByUserId)
                        .note(
                                normalizeNullableText(
                                        request.note()
                                )
                        )
                        .build();

        repairStatusHistoryRepository.save(history);

        return repairOrderMapper.toResponse(
                savedRepairOrder,
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        ),
                repairStatusHistoryRepository
                        .findByRepairOrder_IdOrderByChangedAtAsc(
                                repairOrderId
                        )
        );
    }

    private boolean isValidStatusTransition(
            RepairOrderStatus currentStatus,
            RepairOrderStatus newStatus
    ) {
        return switch (currentStatus) {
            case RECEIVED ->
                    newStatus == RepairOrderStatus.DIAGNOSING
                            || newStatus
                            == RepairOrderStatus.CANCELLED;

            case DIAGNOSING ->
                    newStatus
                            == RepairOrderStatus.WAITING_APPROVAL
                            || newStatus
                            == RepairOrderStatus.IN_PROGRESS
                            || newStatus
                            == RepairOrderStatus.CANCELLED;

            case WAITING_APPROVAL ->
                    newStatus
                            == RepairOrderStatus.IN_PROGRESS
                            || newStatus
                            == RepairOrderStatus.CANCELLED;

            case IN_PROGRESS ->
                    newStatus == RepairOrderStatus.COMPLETED
                            || newStatus
                            == RepairOrderStatus.CANCELLED;

            case COMPLETED ->
                    newStatus == RepairOrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };
    }

    private void validateAllTasksFinished(
            Long repairOrderId
    ) {
        boolean hasUnfinishedTask =
                repairTaskRepository
                        .findByRepairOrder_IdOrderByCreatedAtAsc(
                                repairOrderId
                        )
                        .stream()
                        .anyMatch(task ->
                                task.getStatus()
                                        == RepairTaskStatus.PENDING
                                        || task.getStatus()
                                        == RepairTaskStatus.IN_PROGRESS
                        );

        if (hasUnfinishedTask) {
            throw new InvalidRepairStateException(
                    "Phải hoàn thành hoặc hủy tất cả công việc "
                            + "trước khi hoàn tất phiếu sửa chữa."
            );
        }
    }
    private void validateBookingForRepair(
            BookingDetailsResponse booking
    ) {
        if (!"CONFIRMED".equals(booking.status())) {
            throw new InvalidRepairStateException(
                    "Chỉ có thể tạo phiếu sửa chữa "
                            + "từ lịch hẹn đã được xác nhận."
            );
        }

        if (booking.mechanicUserId() == null) {
            throw new InvalidRepairStateException(
                    "Lịch hẹn chưa được phân công thợ máy."
            );
        }

        if (booking.serviceType() == null
                || booking.serviceType().isBlank()) {
            throw new InvalidRepairStateException(
                    "Lịch hẹn không có loại dịch vụ hợp lệ."
            );
        }
    }

    private String generateRepairCode() {
        String repairCode;

        do {
            String randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

            repairCode = "RO-" + randomPart;
        } while (
                repairOrderRepository.existsByRepairCode(
                        repairCode
                )
        );

        return repairCode;
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}