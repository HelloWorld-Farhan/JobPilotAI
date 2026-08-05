package com.jobpilotai.excel;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.JobApplication;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates professionally styled Excel reports using Apache POI.
 * <p>
 * Features:
 * <ul>
 *   <li>Bold, coloured header row</li>
 *   <li>Auto-sized columns</li>
 *   <li>Alternating row shading</li>
 *   <li>Status colour coding</li>
 *   <li>Metadata sheet with report information</li>
 * </ul>
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public final class ExcelReportGenerator {

    private ExcelReportGenerator() { /* utility class */ }

    private static final String[] HEADERS = {
        "Company", "Website", "Job Title", "Status",
        "Date", "Time", "Resume Used", "Notes", "Attempt Count"
    };

    // Colour palette (ARGB hex)
    private static final String HEADER_BG    = "FF1E3A5F";  // Navy blue
    private static final String HEADER_FG    = "FFFFFFFF";  // White
    private static final String ALT_ROW_BG   = "FFF0F4FA";  // Light blue-grey
    private static final String WHITE        = "FFFFFFFF";

    // Status colours
    private static final String SUCCESS_COLOR  = "FF22C55E";
    private static final String FAILED_COLOR   = "FFEF4444";
    private static final String WARNING_COLOR  = "FFF59E0B";
    private static final String INFO_COLOR     = "FF3B82F6";
    private static final String DEFAULT_COLOR  = "FF64748B";

    /**
     * Generates an Excel report file from the given list of applications.
     *
     * @param applications list of applications to include
     * @param outputFile   destination file (never overwrites; caller must provide unique path)
     * @param reportType   report type label (e.g. MANUAL, HOURLY, FINAL)
     * @throws Exception if file generation fails
     */
    public static void generate(List<JobApplication> applications,
                                File outputFile,
                                String reportType) throws Exception {

        AppLogger.info("Generating Excel report: " + outputFile.getName()
                + " (" + applications.size() + " rows)");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ── Data sheet ────────────────────────────────────────────────
            XSSFSheet dataSheet = workbook.createSheet("Applications");
            dataSheet.setDefaultColumnWidth(20);

            // Title row
            XSSFCellStyle titleStyle = createTitleStyle(workbook);
            XSSFRow titleRow = dataSheet.createRow(0);
            XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("JobPilotAI – " + reportType + " Report");
            titleCell.setCellStyle(titleStyle);
            titleRow.setHeightInPoints(30);
            dataSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // Timestamp row
            XSSFCellStyle tsStyle = createTimestampStyle(workbook);
            XSSFRow tsRow = dataSheet.createRow(1);
            XSSFCell tsCell = tsRow.createCell(0);
            tsCell.setCellValue("Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")));
            tsCell.setCellStyle(tsStyle);
            dataSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, HEADERS.length - 1));

            // Empty row
            dataSheet.createRow(2);

            // Header row
            XSSFRow headerRow = dataSheet.createRow(3);
            headerRow.setHeightInPoints(22);
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < HEADERS.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            XSSFCellStyle evenStyle = createDataStyle(workbook, WHITE);
            XSSFCellStyle oddStyle  = createDataStyle(workbook, ALT_ROW_BG);

            for (int i = 0; i < applications.size(); i++) {
                JobApplication app = applications.get(i);
                XSSFRow row = dataSheet.createRow(4 + i);
                row.setHeightInPoints(18);

                XSSFCellStyle rowStyle = (i % 2 == 0) ? evenStyle : oddStyle;

                setCell(row, 0, app.getCompany(),      rowStyle);
                setCell(row, 1, app.getWebsite(),      rowStyle);
                setCell(row, 2, app.getJobTitle(),     rowStyle);

                // Status cell with colour
                XSSFCell statusCell = row.createCell(3);
                statusCell.setCellValue(app.getStatus());
                statusCell.setCellStyle(createStatusStyle(workbook, app.getStatus()));

                setCell(row, 4, app.getDate(),         rowStyle);
                setCell(row, 5, app.getTime(),         rowStyle);
                setCell(row, 6, app.getResumeUsed(),   rowStyle);
                setCell(row, 7, app.getNotes(),        rowStyle);
                setNumericCell(row, 8, app.getAttemptCount(), rowStyle);
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                dataSheet.autoSizeColumn(i);
                // Pad slightly
                dataSheet.setColumnWidth(i, dataSheet.getColumnWidth(i) + 512);
            }

            // Freeze header
            dataSheet.createFreezePane(0, 4);

            // ── Metadata sheet ────────────────────────────────────────────
            XSSFSheet metaSheet = workbook.createSheet("Report Info");
            XSSFCellStyle metaHeader = createHeaderStyle(workbook);
            XSSFCellStyle metaData   = createDataStyle(workbook, WHITE);

            String[][] meta = {
                {"Report Type",   reportType},
                {"Generated At",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"))},
                {"Total Records", String.valueOf(applications.size())},
                {"File Name",     outputFile.getName()},
                {"Application",   "JobPilotAI v1.0.0"},
            };

            for (int i = 0; i < meta.length; i++) {
                XSSFRow row = metaSheet.createRow(i);
                XSSFCell k = row.createCell(0);
                k.setCellValue(meta[i][0]);
                k.setCellStyle(metaHeader);
                XSSFCell v = row.createCell(1);
                v.setCellValue(meta[i][1]);
                v.setCellStyle(metaData);
            }
            metaSheet.autoSizeColumn(0);
            metaSheet.autoSizeColumn(1);

            // Write to disk
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }

            AppLogger.info("Excel report written to: " + outputFile.getAbsolutePath());
        }
    }

    // ── Style helpers ────────────────────────────────────────────────────────

    private static XSSFCellStyle createTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setColor(new XSSFColor(hexToRgb("FF1E3A5F"), null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle createTimestampStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(new XSSFColor(hexToRgb("FF64748B"), null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRgb(HEADER_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(new XSSFColor(hexToRgb(HEADER_FG), null));
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle createDataStyle(XSSFWorkbook wb, String bgHex) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRgb(bgHex), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(new XSSFColor(hexToRgb("FFCBD5E1"), null));
        return style;
    }

    private static XSSFCellStyle createStatusStyle(XSSFWorkbook wb, String status) {
        String color = switch (status) {
            case "Success"        -> SUCCESS_COLOR;
            case "Failed"         -> FAILED_COLOR;
            case "Pending OTP",
                 "Pending CAPTCHA",
                 "Pending"        -> WARNING_COLOR;
            case "Already Applied"-> INFO_COLOR;
            default               -> DEFAULT_COLOR;
        };
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRgb(color), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(new XSSFColor(hexToRgb("FFFFFFFF"), null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.HAIR);
        return style;
    }

    private static void setCell(XSSFRow row, int col, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static void setNumericCell(XSSFRow row, int col, int value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /** Converts an 8-char ARGB hex string to a 3-byte RGB array. */
    private static byte[] hexToRgb(String argbHex) {
        int argb = (int) Long.parseLong(argbHex, 16);
        return new byte[]{
            (byte) ((argb >> 16) & 0xFF),
            (byte) ((argb >> 8)  & 0xFF),
            (byte)  (argb        & 0xFF)
        };
    }
}
