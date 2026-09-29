package com.autoservice.inventoryservice.service;

import com.autoservice.inventoryservice.domain.entity.InventoryTransaction;
import com.autoservice.inventoryservice.domain.entity.Part;
import com.autoservice.inventoryservice.domain.enums.InventoryTransactionType;
import com.autoservice.inventoryservice.dto.request.CreatePartRequest;
import com.autoservice.inventoryservice.dto.request.StockInRequest;
import com.autoservice.inventoryservice.dto.request.StockOutRequest;
import com.autoservice.inventoryservice.exception.DuplicateResourceException;
import com.autoservice.inventoryservice.exception.InsufficientStockException;
import com.autoservice.inventoryservice.mapper.InventoryMapper;
import com.autoservice.inventoryservice.repository.InventoryTransactionRepository;
import com.autoservice.inventoryservice.repository.PartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private InventoryTransactionRepository
            inventoryTransactionRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private PartService partService;

    @Test
    void stockInShouldIncreaseQuantityAndSaveTransaction() {
        Part part = createPart(20);

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        when(partRepository.save(any(Part.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        StockInRequest request =
                new StockInRequest(
                        10,
                        "PURCHASE_ORDER",
                        1001L,
                        "Nhập thêm phụ tùng."
                );

        partService.stockIn(
                1L,
                4L,
                request
        );

        assertThat(part.getQuantityInStock())
                .isEqualTo(30);

        ArgumentCaptor<InventoryTransaction> captor =
                ArgumentCaptor.forClass(
                        InventoryTransaction.class
                );

        verify(inventoryTransactionRepository)
                .save(captor.capture());

        InventoryTransaction transaction =
                captor.getValue();

        assertThat(transaction.getTransactionType())
                .isEqualTo(
                        InventoryTransactionType.STOCK_IN
                );

        assertThat(transaction.getQuantity())
                .isEqualTo(10);

        assertThat(transaction.getQuantityBefore())
                .isEqualTo(20);

        assertThat(transaction.getQuantityAfter())
                .isEqualTo(30);

        assertThat(transaction.getPerformedByUserId())
                .isEqualTo(4L);
    }

    @Test
    void stockOutShouldDecreaseQuantityAndSaveTransaction() {
        Part part = createPart(20);

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        when(partRepository.save(any(Part.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        StockOutRequest request =
                new StockOutRequest(
                        5,
                        "REPAIR_ORDER",
                        2L,
                        "Xuất kho cho sửa chữa."
                );

        partService.stockOut(
                1L,
                4L,
                request
        );

        assertThat(part.getQuantityInStock())
                .isEqualTo(15);

        ArgumentCaptor<InventoryTransaction> captor =
                ArgumentCaptor.forClass(
                        InventoryTransaction.class
                );

        verify(inventoryTransactionRepository)
                .save(captor.capture());

        InventoryTransaction transaction =
                captor.getValue();

        assertThat(transaction.getTransactionType())
                .isEqualTo(
                        InventoryTransactionType.STOCK_OUT
                );

        assertThat(transaction.getQuantity())
                .isEqualTo(5);

        assertThat(transaction.getQuantityBefore())
                .isEqualTo(20);

        assertThat(transaction.getQuantityAfter())
                .isEqualTo(15);
    }

    @Test
    void stockOutShouldFailWhenStockIsInsufficient() {
        Part part = createPart(4);

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        StockOutRequest request =
                new StockOutRequest(
                        10,
                        "REPAIR_ORDER",
                        3L,
                        "Xuất vượt tồn kho."
                );

        assertThatThrownBy(() ->
                partService.stockOut(
                        1L,
                        4L,
                        request
                )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                )
                .hasMessageContaining(
                        "Tồn kho hiện tại: 4"
                );

        assertThat(part.getQuantityInStock())
                .isEqualTo(4);

        verify(partRepository, never())
                .save(any(Part.class));

        verify(
                inventoryTransactionRepository,
                never()
        ).save(any(InventoryTransaction.class));
    }

    @Test
    void createPartShouldFailWhenPartCodeAlreadyExists() {
        when(
                partRepository
                        .existsByPartCodeIgnoreCase(
                                "BRAKE-PAD-001"
                        )
        ).thenReturn(true);

        CreatePartRequest request =
                new CreatePartRequest(
                        "brake-pad-001",
                        "Má phanh trước",
                        "Má phanh dùng cho bánh trước.",
                        "Akebono",
                        "Bộ",
                        new BigDecimal("850000"),
                        20,
                        5
                );

        assertThatThrownBy(() ->
                partService.createPart(
                        4L,
                        request
                )
        )
                .isInstanceOf(
                        DuplicateResourceException.class
                )
                .hasMessage(
                        "Mã phụ tùng đã tồn tại."
                );

        verify(partRepository, never())
                .save(any(Part.class));

        verify(
                inventoryTransactionRepository,
                never()
        ).save(any(InventoryTransaction.class));
    }

    private Part createPart(
            Integer quantityInStock
    ) {
        return Part.builder()
                .id(1L)
                .partCode("BRAKE-PAD-001")
                .name("Má phanh trước")
                .manufacturer("Akebono")
                .unit("Bộ")
                .unitPrice(
                        new BigDecimal("850000")
                )
                .quantityInStock(
                        quantityInStock
                )
                .minimumStock(5)
                .active(true)
                .build();
    }
}