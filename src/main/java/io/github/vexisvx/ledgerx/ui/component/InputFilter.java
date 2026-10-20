package io.github.vexisvx.ledgerx.ui.component;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class InputFilter {

    /**
     * Allow only digits + one dot + max 2 decimals.
     * Uses textProperty listener (works with setText() too).
     */
    public static void amountField(TextField field) {
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            // allow empty
            if (newV.isEmpty()) return;
            // valid pattern: digits, optionally one dot and 1-2 decimals
            if (!newV.matches("\\d{0,12}(\\.\\d{0,2})?")) {
                // Reject: restore old value, or trim invalid chars
                String cleaned = newV.replaceAll("[^0-9.]", "");
                int dot = cleaned.indexOf('.');
                if (dot >= 0) {
                    // keep only first dot, max 2 decimals
                    String before = cleaned.substring(0, dot);
                    String after = cleaned.substring(dot + 1).replace(".", "");
                    if (after.length() > 2) after = after.substring(0, 2);
                    cleaned = before + "." + after;
                }
                if (cleaned.length() > 12) cleaned = cleaned.substring(0, 12);
                field.setText(cleaned);
            }
        });
    }

    public static void maxLength(TextField field, int max) {
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > max) {
                field.setText(newV.substring(0, max));
            }
        });
    }

    public static void maxLength(TextArea area, int max) {
        area.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > max) {
                area.setText(newV.substring(0, max));
            }
        });
    }

    public static void pin(TextField field) {
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.matches("\\d{0,4}")) {
                field.setText(newV.replaceAll("[^0-9]", ""));
                if (field.getText().length() > 4) {
                    field.setText(field.getText().substring(0, 4));
                }
            }
        });
    }

    public static void intField(TextField field, int maxDigits) {
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.matches("\\d{0," + maxDigits + "}")) {
                field.setText(newV.replaceAll("[^0-9]", ""));
            }
        });
    }
}
