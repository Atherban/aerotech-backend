package com.aerotech.ced_ops_backend.master.line.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "line_master")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Line extends BaseEntity {



    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}