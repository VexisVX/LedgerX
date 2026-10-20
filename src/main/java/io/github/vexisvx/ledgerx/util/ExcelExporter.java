package io.github.vexisvx.ledgerx.util;

import io.github.vexisvx.ledgerx.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelExporter {

    public static void export(File file, List<Transaction> transactions, String language) throws Exception {
        boolean fa = "fa".equals(language);

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = wb.createSheet(fa ? "\u062A\u0631\u0627\u06A9\u0646\u0634\u200C\u0647\u0627" : "Transactions");
            if (fa) sheet.setRightToLeft(true);

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(fa ? HorizontalAlignment.RIGHT : HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);

            // Body cell style
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setAlignment(fa ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);

            // Money style
            CellStyle moneyStyle = wb.createCellStyle();
            moneyStyle.setAlignment(HorizontalAlignment.RIGHT);
            moneyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            CellStyle incomeStyle = wb.createCellStyle();
            incomeStyle.cloneStyleFrom(moneyStyle);
            Font greenFont = wb.createFont();
            greenFont.setColor(IndexedColors.GREEN.getIndex());
            greenFont.setBold(true);
            incomeStyle.setFont(greenFont);

            CellStyle expenseStyle = wb.createCellStyle();
            expenseStyle.cloneStyleFrom(moneyStyle);
            Font redFont = wb.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            redFont.setBold(true);
            expenseStyle.setFont(redFont);

            // Headers
            String[] headers = fa
                ? new String[]{
                    "\u062A\u0627\u0631\u06CC\u062E",
                    "\u0646\u0648\u0639",
                    "\u0639\u0646\u0648\u0627\u0646",
                    "\u062F\u0633\u062A\u0647",
                    "\u062A\u0648\u0636\u06CC\u062D\u0627\u062A",
                    "\u0645\u0628\u0644\u063A"}
                : new String[]{"Date", "Type", "Title", "Category", "Description", "Amount"};

            Row header = sheet.createRow(0);
            header.setHeight((short) 500);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Data
            String incomeText = fa ? "\u062F\u0631\u0622\u0645\u062F" : "Income";
            String expenseText = fa ? "\u0647\u0632\u06CC\u0646\u0647" : "Expense";

            int rowNum = 1;
            for (Transaction t : transactions) {
                Row r = sheet.createRow(rowNum++);

                Cell c0 = r.createCell(0);
                c0.setCellValue(t.getDate().toString());
                c0.setCellStyle(cellStyle);

                Cell c1 = r.createCell(1);
                c1.setCellValue(t.isIncome() ? incomeText : expenseText);
                c1.setCellStyle(cellStyle);

                Cell c2 = r.createCell(2);
                c2.setCellValue(t.getTitle());
                c2.setCellStyle(cellStyle);

                Cell c3 = r.createCell(3);
                c3.setCellValue(t.getCategoryName() != null ? t.getCategoryName() : "-");
                c3.setCellStyle(cellStyle);

                Cell c4 = r.createCell(4);
                c4.setCellValue(t.getDescription() != null ? t.getDescription() : "");
                c4.setCellStyle(cellStyle);

                Cell c5 = r.createCell(5);
                c5.setCellValue(t.getAmount().doubleValue());
                c5.setCellStyle(t.isIncome() ? incomeStyle : expenseStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(w + 800, 15000));
            }

            sheet.createFreezePane(0, 1);
            wb.write(out);
        }
    }

    public static void export(File file, List<Transaction> transactions) throws Exception {
        export(file, transactions, "en");
    }
}
