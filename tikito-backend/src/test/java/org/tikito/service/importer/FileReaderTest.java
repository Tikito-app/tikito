package org.tikito.service.importer;

import org.tikito.dto.ImportFileType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileReaderTest {

    @Test
    void getImportFileType_shouldReturnCsv_givenCsvFile() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("text/csv", ".xlsx"));
        assertEquals(ImportFileType.CSV, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnCsv_givenCsvFileName() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("plain/csv", "name.csv"));
        assertEquals(ImportFileType.CSV, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnExcel_givenXlxFile() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("application/vnd.ms-excel", "name.csv"));
        assertEquals(ImportFileType.EXCEL, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnExcel_givenXlsxFile() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "no-csv-name"));
        assertEquals(ImportFileType.EXCEL, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnExcel_givenXlxName() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("wrong-excel-type", "excel.xlx"));
        assertEquals(ImportFileType.EXCEL, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnExcel_givenXlsxName() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("wrong-excel-type", "excel.xlsx"));
        assertEquals(ImportFileType.EXCEL, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnMT940_givenMT940File() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("random", "name.something", "sdfds\n940\nsdfsd"));
        assertEquals(ImportFileType.MT940, importFileType);
    }

    @Test
    void getImportFileType_shouldReturnNull_givenInvalidFileType() throws IOException {
        final ImportFileType importFileType = FileReader.getImportFileType(getFile("sdf", "sdf"));
        assertNull(importFileType);
    }

    private static MockMultipartFile getFile(final String contentType, final String filename) {
        return getFile(contentType, filename, "sfd");
    }

    private static MockMultipartFile getFile(final String contentType, final String filename, final String content) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                content.getBytes());
    }
}