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

    @FXML private Label lblFabPosition, lblFabValue, lblQuickAdd;
    @FXML private Label lblTitle, lblAppearance, lblTheme, lblLanguage, lblCurrency, lblDateMode,
                        lblSecurity, lblPin, lblBackup, lblCompactMode;
    @FXML private ComboBox<String> themeCombo, langCombo, currencyCombo, dateModeCombo;
    @FXML private CheckBox compactModeCheck, quickAddCheck;
    @FXML private Slider fabOffsetSlider;
    @FXML private Button btnSetPin, btnClearPin, btnBackup, btnRestore, btnSave;

    @FXML
    public void initialize() {
        try {
            setSafe(lblTitle, "settings.title", "Settings");
            setSafe(lblAppearance, "settings.appearance", "Appearance");
            setSafe(lblTheme, "settings.theme", "Theme");
            setSafe(lblLanguage, "settings.language", "Language");
            setSafe(lblCurrency, "settings.currency", "Currency");
            setSafe(lblDateMode, "settings.dateMode", "Date Format");
            setSafe(lblSecurity, "settings.security", "Security");
            setSafe(lblPin, "settings.pin", "PIN Lock");
            setSafe(lblBackup, "settings.backup", "Backup & Restore");
            setSafe(lblCompactMode, "settings.compactMode", "Compact sidebar");

            setBtnSafe(btnSetPin, "settings.pin.set", "Set PIN");
            setBtnSafe(btnClearPin, "settings.pin.clear", "Remove PIN");
            setBtnSafe(btnBackup, "settings.backup.create", "Backup");
            setBtnSafe(btnRestore, "settings.backup.restore", "Restore");
            setBtnSafe(btnSave, "settings.save", "Save");

            if (themeCombo != null) {
                themeCombo.setItems(FXCollections.observableArrayList(
                    "Auto (System)",
                    "Dark Teal", "Dark Purple", "Dark Orange", "Dark Rose", "Dark Slate",
                    "Light Teal", "Light Blue", "Cream"));
                themeCombo.setValue(mapThemeToDisplay(AppConfig.getTheme()));
                themeCombo.valueProperty().addListener((o, a, b) -> {
                    if (b != null && btnSave != null && btnSave.getScene() != null) {
                        try { ThemeManager.setTheme(btnSave.getScene(), mapDisplayToTheme(b)); }
                        catch (Exception ignored) {}
                    }
                });
            }

            if (langCombo != null) {
                langCombo.setItems(FXCollections.observableArrayList("English", "Persian"));
                langCombo.setValue("fa".equals(AppConfig.getLanguage()) ? "Persian" : "English");
            }

            if (currencyCombo != null) {
                currencyCombo.setItems(FXCollections.observableArrayList("USD", "EUR", "IRT", "IRR"));
                currencyCombo.setValue(AppConfig.getCurrency());
            }

            if (dateModeCombo != null) {
                dateModeCombo.setItems(FXCollections.observableArrayList("Gregorian", "Jalali"));
                dateModeCombo.setValue("jalali".equals(AppConfig.getDateMode()) ? "Jalali" : "Gregorian");
            }

            if (compactModeCheck != null) {
                compactModeCheck.setSelected(AppConfig.isCompactMode());
            }
            if (quickAddCheck != null) {
                quickAddCheck.setSelected(AppConfig.isShowQuickAdd());
            }
            if (fabOffsetSlider != null) {
                fabOffsetSlider.setValue(AppConfig.getFabOffset());
                if (lblFabValue != null) lblFabValue.setText((int) fabOffsetSlider.getValue() + " px");
                fabOffsetSlider.valueProperty().addListener((o, a, b) -> {
                    if (lblFabValue != null) lblFabValue.setText((int) b.doubleValue() + " px");
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSafe(Label l, String key, String fallback) {
        if (l == null) return;
        String t = I18n.t(key);
        l.setText(t.startsWith("!") ? fallback : t);
    }

    private void setBtnSafe(Button b, String key, String fallback) {
        if (b == null) return;
        String t = I18n.t(key);
        b.setText(t.startsWith("!") ? fallback : t);
    }

    private String mapThemeToDisplay(String theme) {
        return switch (theme) {
            case "teal" -> "Dark Teal";
            case "purple" -> "Dark Purple";
            case "orange" -> "Dark Orange";
            case "rose" -> "Dark Rose";
            case "slate" -> "Dark Slate";
            case "light-teal" -> "Light Teal";
            case "light-blue" -> "Light Blue";
            case "cream" -> "Cream";
            default -> "Auto (System)";
        };
    }

    private String mapDisplayToTheme(String display) {
        return switch (display) {
            case "Dark Teal" -> "teal";
            case "Dark Purple" -> "purple";
            case "Dark Orange" -> "orange";
            case "Dark Rose" -> "rose";
            case "Dark Slate" -> "slate";
            case "Light Teal" -> "light-teal";
            case "Light Blue" -> "light-blue";
            case "Cream" -> "cream";
            default -> "auto";
        };
    }

    @FXML
    private void save() {
        try {
            if (themeCombo != null && themeCombo.getValue() != null)
                AppConfig.setTheme(mapDisplayToTheme(themeCombo.getValue()));

            if (currencyCombo != null && currencyCombo.getValue() != null)
                AppConfig.setCurrency(currencyCombo.getValue());

            if (dateModeCombo != null && dateModeCombo.getValue() != null)
                AppConfig.setDateMode("Jalali".equals(dateModeCombo.getValue()) ? "jalali" : "gregorian");

            if (compactModeCheck != null) {
                AppConfig.setCompactMode(compactModeCheck.isSelected());
            }
            if (fabOffsetSlider != null) {
                AppConfig.setFabOffset((int) fabOffsetSlider.getValue());
            }
            if (quickAddCheck != null) {
                AppConfig.setShowQuickAdd(quickAddCheck.isSelected());
            }

            if (langCombo != null && langCombo.getValue() != null) {
                String newLang = "English".equals(langCombo.getValue()) ? "en" : "fa";
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
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Set PIN");
        d.setContentText("Enter new 4-digit PIN:");
        d.showAndWait().ifPresent(txt -> {
            if (txt.matches("\\d{4}")) {
                PinService.setPin(txt);
                Toast.success(getWindow(), "PIN saved");
            } else {
                Toast.error(getWindow(), "PIN must be 4 digits");
            }
        });
    }

    @FXML private void clearPin() {
        PinService.clearPin();
        Toast.success(getWindow(), "PIN removed");
    }

    @FXML private void backup() {
        try {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("ledgerx-backup.db");
            File f = fc.showSaveDialog(getWindow());
            if (f != null) {
                BackupService.backup(f);
                Toast.success(getWindow(), "Backup created");
            }
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    @FXML private void restore() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "This will overwrite current data. Continue?");
        if (a.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) return;
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("DB", "*.db"));
            File f = fc.showOpenDialog(getWindow());
            if (f != null) {
                BackupService.restore(f);
                Toast.success(getWindow(), "Restored. Restart app.");
            }
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    private javafx.stage.Window getWindow() {
        if (btnSave != null && btnSave.getScene() != null) return btnSave.getScene().getWindow();
        return null;
    }
}
