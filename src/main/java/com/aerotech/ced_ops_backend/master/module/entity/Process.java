package com.aerotech.ced_ops_backend.master.module.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "module_process", indexes = {
        @Index(name = "idx_process_template_version", columnList = "template_version_id")
}, uniqueConstraints = @UniqueConstraint(name = "uk_process_template_name",
        columnNames = {"template_version_id", "name"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Process extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private TemplateVersion templateVersion;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProcessStatus status;

}