package io.github.vexisvx.ledgerx.ui.component;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class InputFilter {

    /** فقط ۱۲ رقم صحیح + ۲ اعشار — TextFormatter */
    public static void amountField(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            // max 12 digits + optional .XX (max 2 decimals)
            if (newText.matches("\\d{0,12}(\\.\\d{0,2})?")) {
                return change;
            }
            return null; // reject whole change
        }));
    }

    public static void maxLength(TextField field, int max) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    public static void maxLength(TextArea area, int max) {
        area.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    public static void digitsOnly(TextField field, int max) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("\\d{0," + max + "}")) return change;
            return null;
        }));
    }

    public static void pin(TextField field) {
        digitsOnly(field, 4);
    }

    public static void intField(TextField field, int maxDigits) {
        digitsOnly(field, maxDigits);
    }
}
