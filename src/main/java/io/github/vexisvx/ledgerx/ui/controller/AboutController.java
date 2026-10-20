package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.awt.Desktop;
import java.net.URI;

public class AboutController {
    @FXML private Label lblVersion;
    @FXML private Button btnClose;

    @FXML
    public void initialize() {
        lblVersion.setText("v" + AppConfig.APP_VERSION);
        btnClose.setText(I18n.t("about.close"));
    }

    @FXML private void openGitHub() {
        try {
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(AppConfig.GITHUB));
        } catch (Exception ignored) {}
    }

    @FXML private void close() {
        if (btnClose.getScene() != null)
            ((javafx.stage.Stage) btnClose.getScene().getWindow()).close();
    }
}
