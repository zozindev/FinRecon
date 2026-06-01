package com.portfolio.finrecon.api.dto;

import java.time.LocalDate;
import java.util.Map;

import com.portfolio.finrecon.domain.ReconciliationResultType;

public record ReconciliationSummaryResponse(
        LocalDate businessDate,
        long totalCount,
        Map<ReconciliationResultType, Long> counts) {
}
