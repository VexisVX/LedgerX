package io.github.vexisvx.ledgerx.util;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

public class AlertHelper {

    private static void styleDialog(DialogPane pane) {
        if (pane == null) return;
        try {
            var base = AlertHelper.class.getResource("/css/base.css");
            var theme = AlertHelper.class.getResource("/css/theme-" + AppConfig.getTheme() + ".css");
            if (base != null) pane.getStylesheets().add(base.toExternalForm());
            if (theme != null) pane.getStylesheets().add(theme.toExternalForm());
        } catch (Exception ignored) {}
    }

    public static void success(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(I18n.t("alert.success"));
        a.setHeaderText(null);
        a.setContentText(m);
        styleDialog(a.getDialogPane());
        a.showAndWait();
    }

    public static void error(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(I18n.t("alert.error"));
        a.setHeaderText(null);
        a.setContentText(m);
        styleDialog(a.getDialogPane());
        a.showAndWait();
    }

    public static boolean confirm(String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(I18n.t("alert.confirm"));
        a.setHeaderText(null);
        a.setContentText(m);
        styleDialog(a.getDialogPane());
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    /** Convenience for custom styled dialogs. */
    public static void applyTheme(DialogPane pane) {
        styleDialog(pane);
    }
}
