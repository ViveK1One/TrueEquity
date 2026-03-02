package com.trueequity.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock basic information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockInfoDTO {
    private String symbol;
    private String name;
    private String exchange;
    private String sector;
    private String industry;
    private Long marketCap;
}

