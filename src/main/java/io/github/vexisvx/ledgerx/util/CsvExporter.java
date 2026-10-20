package io.github.vexisvx.ledgerx.util;

import io.github.vexisvx.ledgerx.model.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvExporter {

    public static void export(File f, List<Transaction> list, String language) throws Exception {
        boolean fa = "fa".equals(language);

        String[] headers = fa
            ? new String[]{"تاریخ", "نوع", "عنوان", "دسته", "توضیحات", "مبلغ"}
            : new String[]{"Date", "Type", "Title", "Category", "Description", "Amount"};

        String incomeText = fa ? "درآمد" : "Income";
        String expenseText = fa ? "هزینه" : "Expense";

        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            w.write('\ufeff'); // BOM for Excel

            // Header
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) w.write(",");
                w.write("\"" + esc(headers[i]) + "\"");
            }
            w.write("\n");

            // Rows
            for (Transaction t : list) {
                w.write(esc(t.getDate().toString()) + ",");
                w.write("\"" + esc(t.isIncome() ? incomeText : expenseText) + "\",");
                w.write("\"" + esc(t.getTitle()) + "\",");
                w.write("\"" + esc(t.getCategoryName() != null ? t.getCategoryName() : "") + "\",");
                w.write("\"" + esc(t.getDescription() != null ? t.getDescription() : "") + "\",");
                w.write(t.getAmount().toPlainString());
                w.write("\n");
            }
        }
    }

    // Backward-compatible
    public static void export(File f, List<Transaction> list) throws Exception {
        export(f, list, "en");
    }

    private static String esc(String s) { return s == null ? "" : s.replace("\"", "\"\""); }
}
