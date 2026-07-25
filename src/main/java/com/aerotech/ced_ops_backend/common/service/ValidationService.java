package com.aerotech.ced_ops_backend.common.service;

import com.aerotech.ced_ops_backend.common.enums.InspectionResult;
import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.master.parameter.entity.ParameterMaster;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class ValidationService {

    public InspectionResult validate(
            ParameterMaster parameter,
            String observedValue
    ) {

        if (observedValue == null || observedValue.isBlank()) {
            return InspectionResult.NOT_APPLICABLE;
        }

        if (Objects.requireNonNull(parameter.getInputType()) == InputType.NUMBER) {
            try {

                BigDecimal value = new BigDecimal(observedValue);

                if (parameter.getMinValue() != null
                        && value.compareTo(parameter.getMinValue()) < 0) {
                    return InspectionResult.FAIL;
                }

                if (parameter.getMaxValue() != null
                        && value.compareTo(parameter.getMaxValue()) > 0) {
                    return InspectionResult.FAIL;
                }

                return InspectionResult.PASS;

            } catch (NumberFormatException ex) {

                return InspectionResult.FAIL;

            }
        }
        return InspectionResult.NOT_APPLICABLE;

    }

}