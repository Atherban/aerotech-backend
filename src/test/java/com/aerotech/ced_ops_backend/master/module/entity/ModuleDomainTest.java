package com.aerotech.ced_ops_backend.master.module.entity;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import com.aerotech.ced_ops_backend.master.module.enums.TemplateVersionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleDomainTest {

    @Test
    void moduleBuilderSetsLifecycleAndDefaults() {
        ModuleType type = ModuleType.builder().name("Quality").active(true).build();

        Module module = Module.builder()
                .moduleType(type)
                .name("First Piece Inspection")
                .prefix("FPI")
                .status(ModuleStatus.DRAFT)
                .build();

        assertThat(module.getName()).isEqualTo("First Piece Inspection");
        assertThat(module.getPrefix()).isEqualTo("FPI");
        assertThat(module.getStatus()).isEqualTo(ModuleStatus.DRAFT);
        assertThat(module.getModuleType().getName()).isEqualTo("Quality");
    }

    @Test
    void processPreservesConfiguredDisplayOrder() {
        TemplateVersion version = TemplateVersion.builder()
                .versionNumber(1)
                .status(TemplateVersionStatus.ACTIVE)
                .build();

        Process second = Process.builder()
                .templateVersion(version)
                .name("CED Coating")
                .displayOrder(2)
                .status(ProcessStatus.ACTIVE)
                .build();

        Process first = Process.builder()
                .templateVersion(version)
                .name("Shot Blasting")
                .displayOrder(1)
                .status(ProcessStatus.ACTIVE)
                .build();

        assertThat(first.getDisplayOrder()).isLessThan(second.getDisplayOrder());
        assertThat(first.getDisplayOrder()).isEqualTo(1);
        assertThat(second.getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void processParameterCarriesPerProcessSpecification() {
        Parameter parameter = Parameter.builder()
                .name("Temperature")
                .inputType(InputType.NUMBER)
                .build();

        ProcessParameter spec = ProcessParameter.builder()
                .parameter(parameter)
                .displayOrder(1)
                .mandatory(true)
                .visible(true)
                .unit("°C")
                .minimumValue(new BigDecimal("60"))
                .maximumValue(new BigDecimal("75"))
                .active(true)
                .build();

        assertThat(spec.getUnit()).isEqualTo("°C");
        assertThat(spec.getMinimumValue()).isEqualByComparingTo("60");
        assertThat(spec.getMaximumValue()).isEqualByComparingTo("75");
        assertThat(spec.getMandatory()).isTrue();
        assertThat(spec.getVisible()).isTrue();
        assertThat(spec.getParameter().getName()).isEqualTo("Temperature");
        assertThat(spec.getParameter().getInputType()).isEqualTo(InputType.NUMBER);
    }

    @Test
    void processParameterDefaultsAreSensible() {
        ProcessParameter spec = ProcessParameter.builder().displayOrder(1).build();

        assertThat(spec.getMandatory()).isTrue();
        assertThat(spec.getVisible()).isTrue();
        assertThat(spec.getActive()).isTrue();
    }
}