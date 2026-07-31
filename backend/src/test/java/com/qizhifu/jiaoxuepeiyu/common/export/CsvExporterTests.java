package com.qizhifu.jiaoxuepeiyu.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class CsvExporterTests {

    @Test
    void writesUtf8BomAndEscapesCsvValues() {
        byte[] bytes = CsvExporter.toCsvBytes(
                Arrays.asList("Name", "Remark", "Empty"),
                Arrays.asList(Arrays.asList("Alice", "hello, \"world\"\nnext", null)));

        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);
        String csv = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);

        assertEquals("Name,Remark,Empty\r\nAlice,\"hello, \"\"world\"\"\nnext\",\r\n", csv);
    }

    @Test
    void writesOnlyHeaderWhenRowsAreEmpty() {
        byte[] bytes = CsvExporter.toCsvBytes(Arrays.asList("A", "B"), null);
        String csv = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);

        assertTrue(csv.endsWith("\r\n"));
        assertEquals("A,B\r\n", csv);
    }

    @Test
    void createsCsvAttachmentResponse() {
        ResponseEntity<byte[]> response = CsvExporter.toAttachment(
                "accounts.csv",
                Arrays.asList("A"),
                Arrays.asList(Arrays.asList("B")));

        assertEquals("attachment; filename=\"accounts.csv\"",
                response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("text/csv;charset=UTF-8", response.getHeaders().getContentType().toString());
    }
}
