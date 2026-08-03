package com.aerotech.ced_ops_backend.master.shift.entity;

import com.aerotech.ced_ops_backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

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

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    public boolean covers(LocalTime time) {
        if (time == null || startTime == null || endTime == null) {
            return false;
        }
        if (startTime.isBefore(endTime)) {
            return !time.isBefore(startTime) && time.isBefore(endTime);
        }
        return !time.isBefore(startTime) || time.isBefore(endTime);
    }

}
