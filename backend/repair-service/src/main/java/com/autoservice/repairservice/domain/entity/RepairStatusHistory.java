package com.autoservice.repairservice.domain.entity;

import com.autoservice.repairservice.domain.enums.RepairOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "repair_order_id",
            nullable = false
    )
    private RepairOrder repairOrder;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "previous_status",
            length = 40
    )
    private RepairOrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "new_status",
            nullable = false,
            length = 40
    )
    private RepairOrderStatus newStatus;

    @Column(
            name = "changed_by_user_id",
            nullable = false
    )
    private Long changedByUserId;

    @Column(
            name = "note",
            length = 1000
    )
    private String note;

    @CreationTimestamp
    @Column(
            name = "changed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime changedAt;
}