package com.portfolio.finrecon.domain;

public enum ReconciliationResultType {
    MATCHED,
    LEDGER_MISSING,
    TRANSACTION_MISSING,
    AMOUNT_MISMATCH,
    STATUS_MISMATCH
}
