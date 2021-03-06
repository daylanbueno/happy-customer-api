package com.github.daylanbueno.happycustomer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private LocalDate dateTransaction;
    private BigDecimal total;
    private Integer totalPoint;
}
