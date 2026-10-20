package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.ui.component.Toast;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.ui.theme.ThemeManager;
import io.github.vexisvx.ledgerx.util.BackupService;
import io.github.vexisvx.ledgerx.util.PinService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class SettingsController {

    @FXML private Label lblDisplayMode;
    @FXML private Label lblTitle, lblAppearance, lblTheme, lblLanguage, lblCurrency, lblDateMode,
                        lblSecurity, lblPin, lblBackup;
    @FXML private ComboBox<String> themeCombo, langCombo, currencyCombo, dateModeCombo, displayModeCombo;
    @FXML private Button btnSetPin, btnClearPin, btnBackup, btnRestore, btnSave;

    private static final String[][] THEMES_DISPLAY = {
        {"auto",       "\uD83C\uDF13  Auto (System)"},
        {"teal",       "\uD83D\uDFE2  Dark Teal"},
        {"purple",     "\uD83D\uDFE3  Dark Purple"},
        {"orange",     "\uD83D\uDFE0  Dark Orange"},
        {"rose",       "\uD83C\uDF39  Dark Rose"},
        {"slate",      "\u26AA  Dark Slate"},
        {"light-teal", "\u2600\uFE0F  Light Teal"},
        {"light-blue", "\uD83D\uDD35  Light Blue"},
        {"cream",      "\uD83D\uDFE1  Cream"}
    };

    @FXML
    public void initialize() {
        try {
            safe(lblTitle, "settings.title");
            safe(lblAppearance, "settings.appearance");
            safe(lblTheme, "settings.theme");
            safe(lblLanguage, "settings.language");
            safe(lblCurrency, "settings.currency");
            safe(lblDateMode, "settings.dateMode");
            safe(lblSecurity, "settings.security");
            safe(lblPin, "settings.pin");
            safe(lblBackup, "settings.backup");

            if (btnSetPin != null) btnSetPin.setText(I18n.t("settings.pin.set"));
            if (btnClearPin != null) btnClearPin.setText(I18n.t("settings.pin.clear"));
            if (btnBackup != null) btnBackup.setText(I18n.t("settings.backup.create"));
            if (btnRestore != null) btnRestore.setText(I18n.t("settings.backup.restore"));
            if (btnSave != null) btnSave.setText(I18n.t("settings.save"));

            if (themeCombo != null) {
                themeCombo.setItems(FXCollections.observableArrayList(
                    "\uD83C\uDF13  Auto (System)",
                    "\uD83D\uDFE2  Dark Teal",
                    "\uD83D\uDFE3  Dark Purple",
                    "\uD83D\uDFE0  Dark Orange",
                    "\uD83C\uDF39  Dark Rose",
                    "\u26AA  Dark Slate",
                    "\u2600\uFE0F  Light Teal",
                    "\uD83D\uDD35  Light Blue",
                    "\uD83D\uDFE1  Cream"));
                String currentDisplay = displayFor(AppConfig.getTheme());
                if (currentDisplay != null) themeCombo.setValue(currentDisplay);

                themeCombo.valueProperty().addListener((o, a, b) -> {
                    if (b == null) return;
                    String themeKey = keyFor(b);
                    if (themeKey != null) ThemeManager.setTheme(themeKey);
                });
            }

            if (langCombo != null) {
                langCombo.setItems(FXCollections.observableArrayList("English", "Persian"));
                langCombo.setValue("en".equals(AppConfig.getLanguage()) ? "English" : "Persian");
            }
            if (currencyCombo != null) {
                currencyCombo.setItems(FXCollections.observableArrayList("USD", "EUR", "IRT", "IRR"));
                currencyCombo.setValue(AppConfig.getCurrency());
            }
            if (dateModeCombo != null) {
                dateModeCombo.setItems(FXCollections.observableArrayList("Gregorian", "Jalali"));
                dateModeCombo.setValue("jalali".equals(AppConfig.getDateMode()) ? "Jalali" : "Gregorian");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void safe(Label l, String key) { if (l != null) l.setText(I18n.t(key)); }

    private String displayFor(String key) {
        for (String[] pair : THEMES_DISPLAY) if (pair[0].equals(key)) return pair[1];
        return null;
    }

    private String keyFor(String display) {
        for (String[] pair : THEMES_DISPLAY) if (pair[1].equals(display)) return pair[0];
        return null;
    }

    @FXML private void save() {
        try {
            String themeDisplay = themeCombo != null ? themeCombo.getValue() : null;
            String themeKey = keyFor(themeDisplay);
            if (themeKey != null) AppConfig.setTheme(themeKey);

            if (currencyCombo != null && currencyCombo.getValue() != null)
                AppConfig.setCurrency(currencyCombo.getValue());

            if (dateModeCombo != null && dateModeCombo.getValue() != null) {
                boolean jalali = "Jalali".equals(dateModeCombo.getValue());
                AppConfig.setDateMode(jalali ? "jalali" : "gregorian");

            if (displayModeCombo != null && displayModeCombo.getValue() != null) {
                String iconText = I18n.t("settings.displayMode.icon");
                AppConfig.setDisplayMode(displayModeCombo.getValue().equals(iconText) ? "icon" : "text");
            }
            }

            String langDisplay = langCombo != null ? langCombo.getValue() : null;
            if (langDisplay != null) {
                String newLang = "English".equals(langDisplay) ? "en" : "fa";
                I18n.setLanguage(newLang);
            }

            Toast.success(getWindow(), "Settings saved. Restarting...");

            PauseTransition pause = new PauseTransition(Duration.millis(900));
            pause.setOnFinished(e -> {
                Platform.exit();
                System.exit(0);
            });
            pause.play();
        } catch (Exception e) {
            Toast.error(getWindow(), e.getMessage());
        }
    }

    @FXML private void setPin() {
        try {
            TextInputDialog d = new TextInputDialog();
            d.setTitle(I18n.t("settings.pin.set"));
            d.setContentText(I18n.t("settings.pin.prompt"));
            d.showAndWait().ifPresent(txt -> {
                if (isValidPin(txt)) {
                    PinService.setPin(txt);
                    Toast.success(getWindow(), I18n.t("settings.pin.saved"));
                } else {
                    Toast.error(getWindow(), I18n.t("settings.pin.wrong"));
                }
            });
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    private boolean isValidPin(String txt) {
        if (txt == null || txt.length() != 4) return false;
        for (int i = 0; i < 4; i++) if (!Character.isDigit(txt.charAt(i))) return false;
        return true;
    }

    @FXML private void clearPin() {
        PinService.clearPin();
        Toast.success(getWindow(), I18n.t("settings.pin.removed"));
    }

    @FXML private void backup() {
        try {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("ledgerx-backup.db");
            File f = fc.showSaveDialog(getWindow());
            if (f != null) { BackupService.backup(f); Toast.success(getWindow(), I18n.t("settings.backup.done")); }
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    @FXML private void restore() {
        try {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, I18n.t("settings.backup.warning"));
            if (a.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) return;
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("DB", "*.db"));
            File f = fc.showOpenDialog(getWindow());
            if (f != null) { BackupService.restore(f); Toast.success(getWindow(), I18n.t("settings.backup.restored")); }
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    private javafx.stage.Window getWindow() {
        if (btnSave != null && btnSave.getScene() != null) return btnSave.getScene().getWindow();
        return null;
    }
}
