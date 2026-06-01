package com.portfolio.finrecon.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.portfolio.finrecon.common.exception.DomainException;

@Component
public class CsvFileReader {

    public List<CsvRow> read(byte[] content, List<String> expectedHeader) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw invalidFile("CSV file is empty.");
            }
            List<String> header = List.of(headerLine.split(",", -1));
            if (!header.equals(expectedHeader)) {
                throw invalidFile("CSV header does not match the expected format.");
            }

            List<CsvRow> rows = new ArrayList<>();
            String line;
            int rowNumber = 2;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    rowNumber++;
                    continue;
                }
                String[] values = line.split(",", -1);
                if (values.length != header.size()) {
                    throw invalidFile("CSV row " + rowNumber + " has an invalid column count.");
                }
                Map<String, String> fields = new LinkedHashMap<>();
                for (int i = 0; i < header.size(); i++) {
                    fields.put(header.get(i), values[i].trim());
                }
                rows.add(new CsvRow(rowNumber, fields));
                rowNumber++;
            }
            return rows;
        } catch (IOException exception) {
            throw invalidFile("CSV file cannot be read.");
        }
    }

    private DomainException invalidFile(String message) {
        return new DomainException(HttpStatus.BAD_REQUEST, "INVALID_CSV_FILE", message);
    }

    public record CsvRow(int rowNumber, Map<String, String> fields) {
        public String get(String name) {
            return fields.getOrDefault(name, "");
        }
    }
}
