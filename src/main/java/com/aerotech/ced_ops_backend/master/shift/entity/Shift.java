package com.aerotech.ced_ops_backend.master.shift.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shift extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}