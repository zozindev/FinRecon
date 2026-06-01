package com.portfolio.finrecon.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record RunDateRequest(@NotNull LocalDate businessDate) {
}
