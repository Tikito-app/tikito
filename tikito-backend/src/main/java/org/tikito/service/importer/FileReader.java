package org.tikito.service.importer;

import org.tikito.dto.ImportFileType;
import org.tikito.exception.CannotReadFileException;
import org.tikito.util.Util;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FileReader {

    // visible for testing
    public static ImportFileType getImportFileType(final MultipartFile file) throws IOException {
        if ("text/csv".equals(file.getContentType())) {
            return ImportFileType.CSV;
        } else if ("application/vnd.ms-excel".equals(file.getContentType()) || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType())) {
            return ImportFileType.EXCEL;
        }
        final String content = new String(file.getBytes());
        final String[] lines = content.split("\n");
        if (lines.length > 1) {
            if ("MT940".equals(lines[1]) || "940".equals(lines[1])) {
                return ImportFileType.MT940;
            }
        }
        if (file.getOriginalFilename() != null) {
            final String originalFilename = file.getOriginalFilename().toLowerCase();

            if (originalFilename.endsWith(".csv")) {
                return ImportFileType.CSV;
            }
            if (originalFilename.endsWith(".xlsx") || originalFilename.endsWith(".xlx")) {
                return ImportFileType.EXCEL;
            }
            if (originalFilename.endsWith(".sta")) {
                return ImportFileType.MT940;
            }
        }
        return null;
    }

    public static List<List<String>> readExcel(final MultipartFile file) throws CannotReadFileException {
        try {
            return readExcel(file.getInputStream(), Util.getFileExtension(file.getOriginalFilename()));
        } catch (final IOException e) {
            throw new CannotReadFileException();
        }
    }

    public static List<List<String>> readExcel(final InputStream inputStream, final String extension) throws CannotReadFileException {
        final Workbook workbook;
        try {
            workbook = switch (extension.toLowerCase()) {
                case "xlsx" -> new XSSFWorkbook(inputStream);
                case "xls" -> new HSSFWorkbook(inputStream);
                default -> throw new IllegalStateException("Unexpected value: " + extension.toLowerCase());
            };
        } catch (final IOException e) {
            throw new CannotReadFileException();
        }

        final Sheet sheet = workbook.getSheetAt(0);
        final List<List<String>> data = new ArrayList<>();

        int rowCount = 0;
        for (final Row row : sheet) {
            final List<String> rowData = new ArrayList<>();
            for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
                final String cellValue = getCellValue(row.getCell(i));
                if (rowCount != 0 || cellValue != null) {
                    rowData.add(cellValue);
                }
            }
            data.add(rowData);
            rowCount++;
        }

        return data;
    }

    private static String getCellValue(final Cell cell) {
        if (cell == null) {
            return null;
        }
        final DataFormatter dataFormatter = new DataFormatter();
        return dataFormatter.formatCellValue(cell);
    }

    public static List<List<String>> readCsv(final MultipartFile multipartFile, final char separatorChar, final char quoteChar) throws CannotReadFileException {
        try {
            return readCsv(multipartFile.getInputStream(), separatorChar, quoteChar);
        } catch (final IOException e) {
            throw new CannotReadFileException();
        }
    }

    public static List<List<String>> readCsv(final InputStream inputStream, final char separatorChar, final char quoteChar) throws CannotReadFileException {
        final CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(separatorChar)
                .withQuoteChar(quoteChar)
                .build();

        try (final CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                .withSkipLines(0)
                .withCSVParser(csvParser)
                .build()) {

            return new ArrayList<>(csvReader
                    .readAll()
                    .stream()
                    .map(Arrays::asList)
                    .toList());
        } catch (final IOException | CsvException e) {
            throw new CannotReadFileException();
        }
    }
}
