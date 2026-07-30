package com.qizhifu.jiaoxuepeiyu.common.export;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static byte[] toCsvBytes(List<String> headers, List<List<String>> rows) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, headers);
        if (rows != null) {
            for (List<String> row : rows) {
                appendRow(csv, row);
            }
        }
        byte[] content = csv.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream(content.length + 3);
        output.write(0xEF);
        output.write(0xBB);
        output.write(0xBF);
        output.write(content, 0, content.length);
        return output.toByteArray();
    }

    public static ResponseEntity<byte[]> toAttachment(String filename, List<String> headers, List<List<String>> rows) {
        String safeName = filename == null || filename.trim().length() == 0 ? "export.csv" : filename.trim();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName.replace("\"", "") + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(toCsvBytes(headers, rows));
    }

    private static void appendRow(StringBuilder csv, List<String> values) {
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    csv.append(',');
                }
                csv.append(escape(values.get(i)));
            }
        }
        csv.append("\r\n");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean quote = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        String escaped = value.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }
}
