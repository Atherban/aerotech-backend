package com.aerotech.ced_ops_backend.master.module.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import com.aerotech.ced_ops_backend.common.enums.InputType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parameter")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameter extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InputType inputType;

    @Column(length = 300)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}