package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerGrowthPointDTO {
    private String month; // "YYYY-MM"
    private long newCustomers;
    private long cumulativeCustomers;
}
