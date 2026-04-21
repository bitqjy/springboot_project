package com.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 简单Excel读取工具：第一行做表头，后续行做数据。
 * 仅用于系统内“成绩导入/名单导入”等场景，不追求复杂格式。
 */
public class ExcelImportUtil {

    public static class SheetData {
        public final List<String> headers;
        public final List<Map<String, String>> rows;

        public SheetData(List<String> headers, List<Map<String, String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }
    }

    public static SheetData readFirstSheet(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("导入文件为空");
        }
        String name = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.isNotBlank(name) && name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }

        if ("csv".equals(ext)) {
            return readCsv(file);
        }
        if ("xls".equals(ext) || "xlsx".equals(ext)) {
            return readExcel(file, ext);
        }
        throw new IllegalArgumentException("不支持的导入格式，仅支持xls/xlsx/csv");
    }

    private static SheetData readExcel(MultipartFile file, String ext) throws IOException {
        try (InputStream in = file.getInputStream()) {
            Workbook workbook = "xls".equals(ext) ? new HSSFWorkbook(in) : new XSSFWorkbook(in);
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("Excel中找不到sheet");
            }

            int firstRowNum = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(firstRowNum);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel表头为空");
            }

            int lastCellNum = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < lastCellNum; c++) {
                String hv = CommonUtil.getCellValue(headerRow.getCell(c));
                headers.add(normalizeHeader(hv));
            }

            List<Map<String, String>> rows = new ArrayList<>();
            int lastRowNum = sheet.getLastRowNum();
            for (int r = firstRowNum + 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> m = new HashMap<>();
                boolean allBlank = true;
                for (int c = 0; c < lastCellNum; c++) {
                    String key = headers.get(c);
                    if (StringUtils.isBlank(key)) {
                        continue;
                    }
                    String v = CommonUtil.getCellValue(row.getCell(c));
                    if (StringUtils.isNotBlank(v)) {
                        allBlank = false;
                    }
                    m.put(key, StringUtils.trimToEmpty(v));
                }
                if (!allBlank) {
                    rows.add(m);
                }
            }
            return new SheetData(headers, rows);
        }
    }

    private static SheetData readCsv(MultipartFile file) throws IOException {
        // 优先按UTF-8读取，若出现明显乱码字符则回退GBK
        SheetData utf8 = readCsvWithCharset(file, StandardCharsets.UTF_8);
        if (looksGarbled(utf8.headers)) {
            return readCsvWithCharset(file, Charset.forName("GBK"));
        }
        return utf8;
    }

    private static SheetData readCsvWithCharset(MultipartFile file, Charset charset) throws IOException {
        List<String> headers = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), charset))) {
            String line;
            boolean headerRead = false;
            while ((line = br.readLine()) != null) {
                if (!headerRead) {
                    List<String> headerCells = parseCsvLine(line);
                    for (String cell : headerCells) {
                        headers.add(normalizeHeader(cell));
                    }
                    if (headers.isEmpty()) {
                        throw new IllegalArgumentException("CSV表头为空");
                    }
                    headerRead = true;
                    continue;
                }
                List<String> cells = parseCsvLine(line);
                Map<String, String> m = new HashMap<>();
                boolean allBlank = true;
                for (int c = 0; c < headers.size(); c++) {
                    String key = headers.get(c);
                    if (StringUtils.isBlank(key)) {
                        continue;
                    }
                    String v = c < cells.size() ? StringUtils.trimToEmpty(cells.get(c)) : "";
                    if (StringUtils.isNotBlank(v)) {
                        allBlank = false;
                    }
                    m.put(key, v);
                }
                if (!allBlank) {
                    rows.add(m);
                }
            }
        }

        if (headers.isEmpty()) {
            throw new IllegalArgumentException("CSV表头为空");
        }
        return new SheetData(headers, rows);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) {
            return result;
        }
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private static boolean looksGarbled(List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            return false;
        }
        for (String h : headers) {
            if (h != null && h.contains("�")) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeHeader(String s) {
        if (s == null) {
            return "";
        }
        String v = s.replace("\uFEFF", "");
        return v.trim().replaceAll("\\s+", "");
    }
}
