package com.portfolio.finrecon.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
        "delete from audit_logs",
        "delete from batch_executions",
        "delete from settlements",
        "delete from reconciliation_results",
        "delete from ledger_entries",
        "delete from validation_errors",
        "delete from external_transactions",
        "delete from uploaded_files"
})
class FinReconWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadsReconcilesAndSettlesDailyTransactions() throws Exception {
        String token = login("operator", "operator123!");

        mockMvc.perform(multipart("/api/v1/transaction-files")
                .file(csv("file", "transactions.csv", """
                        transactionId,transactionType,originalTransactionId,merchantId,transactionDate,amount,maskedPaymentNumber,status
                        TXN-20260601-0001,APPROVAL,,MERCHANT-001,2026-06-01,12000.00,****-****-****-1234,APPROVED
                        TXN-20260601-0002,CANCEL,TXN-20260601-0001,MERCHANT-001,2026-06-01,2000.00,****-****-****-1234,CANCELED
                        """))
                .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.validCount").value(2))
                .andExpect(jsonPath("$.data.errorCount").value(0));

        mockMvc.perform(multipart("/api/v1/ledger-files")
                .file(csv("file", "ledger-entries.csv", """
                        ledgerReferenceId,transactionId,merchantId,recordDate,amount,status
                        LEDGER-0001,TXN-20260601-0001,MERCHANT-001,2026-06-01,12000.00,APPROVED
                        LEDGER-0002,TXN-20260601-0002,MERCHANT-001,2026-06-01,2000.00,CANCELED
                        """))
                .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.validCount").value(2))
                .andExpect(jsonPath("$.data.errorCount").value(0));

        mockMvc.perform(post("/api/v1/reconciliations")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessDate\":\"2026-06-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].resultType").value("MATCHED"))
                .andExpect(jsonPath("$.data[1].resultType").value("MATCHED"));

        mockMvc.perform(get("/api/v1/reconciliations/summary")
                .header("Authorization", bearer(token))
                .param("businessDate", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.counts.MATCHED").value(2));

        mockMvc.perform(post("/api/v1/settlements/daily")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessDate\":\"2026-06-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approvalAmount").value(12000.00))
                .andExpect(jsonPath("$.data.cancellationAmount").value(2000.00))
                .andExpect(jsonPath("$.data.grossAmount").value(10000.00))
                .andExpect(jsonPath("$.data.feeAmount").value(250.00))
                .andExpect(jsonPath("$.data.payoutAmount").value(9750.00));

        mockMvc.perform(get("/api/v1/batch-executions")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data[1].executionStatus").value("SUCCESS"));
    }

    @Test
    void storesInvalidTransactionRowsAsValidationErrors() throws Exception {
        String token = login("operator", "operator123!");

        mockMvc.perform(multipart("/api/v1/transaction-files")
                .file(csv("file", "invalid-transactions.csv", """
                        transactionId,transactionType,originalTransactionId,merchantId,transactionDate,amount,maskedPaymentNumber,status
                        TXN-INVALID-0001,APPROVAL,,MERCHANT-001,2026-06-01,-100.00,****-****-****-1234,APPROVED
                        """))
                .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.validCount").value(0))
                .andExpect(jsonPath("$.data.errorCount").value(1));

        mockMvc.perform(get("/api/v1/validation-errors")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].errorCode").value("INVALID_AMOUNT"));
    }

    @Test
    void rejectsNonCsvUploads() throws Exception {
        String token = login("operator", "operator123!");

        mockMvc.perform(multipart("/api/v1/transaction-files")
                .file(new MockMultipartFile(
                        "file",
                        "transactions.txt",
                        "text/plain",
                        "not,csv".getBytes(StandardCharsets.UTF_8)))
                .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILE_TYPE"));
    }

    @Test
    void protectsBusinessApisAndRequiresAdminForAuditLogs() throws Exception {
        String operatorToken = login("operator", "operator123!");
        String adminToken = login("admin", "admin123!");

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/transactions")
                .header("Authorization", "Bearer malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", bearer(operatorToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void exposesOpenApiSpecWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/openapi.yaml"))
                .andExpect(status().isOk());
    }

    private MockMultipartFile csv(String name, String filename, String content) {
        return new MockMultipartFile(
                name,
                filename,
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private String login(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.data.accessToken");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
