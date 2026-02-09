package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyUserRegistrationsDTO {
    private String date;
    private long free;
    private long premium;
    private long enterprise;
}
