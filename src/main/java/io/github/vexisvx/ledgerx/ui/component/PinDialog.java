package io.github.vexisvx.ledgerx.ui.component;

import io.github.vexisvx.ledgerx.util.PinService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class PinDialog {

    private static int attempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    /** Shows PIN dialog. Returns true if unlocked, false if user cancelled/quits. */
    public static boolean show() {
        if (!PinService.isEnabled()) return true;
        attempts = 0;

        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);

        final boolean[] result = {false};

        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #0b1220, #111a28);" +
            "-fx-border-color: rgba(0,212,170,0.4);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;"
        );

        Label icon = new Label("\uD83D\uDD12");
        icon.setStyle("-fx-font-size: 42px;");

        Label title = new Label("LedgerX");
        title.setFont(Font.font("System", FontWeight.BLACK, 26));
        title.setTextFill(Color.web("#00d4aa"));

        Label prompt = new Label("Enter 4-digit PIN");
        prompt.setTextFill(Color.web("#a0b0c0"));

        PasswordField field = new PasswordField();
        field.setPromptText("\u2022 \u2022 \u2022 \u2022");
        field.setMaxWidth(180);
        field.setStyle(
            "-fx-background-color: #1a222c;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-alignment: CENTER;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #2a3a4a;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10 12;"
        );

        Label error = new Label("");
        error.setTextFill(Color.web("#ff6b6b"));
        error.setFont(Font.font(12));

        Button unlock = new Button("Unlock");
        unlock.setStyle(
            "-fx-background-color: linear-gradient(135deg, #00d4aa, #0092ff);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 30;" +
            "-fx-cursor: hand;"
        );

        Button exit = new Button("Exit");
        exit.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a0b0c0;" +
            "-fx-cursor: hand;"
        );

        HBox buttons = new HBox(10, exit, unlock);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(icon, title, prompt, field, error, buttons);

        Runnable tryUnlock = () -> {
            String pin = field.getText();
            if (pin == null || !pin.matches("\\d{4}")) {
                error.setText("PIN must be 4 digits");
                field.clear();
                return;
            }
            if (PinService.verify(pin)) {
                result[0] = true;
                stage.close();
            } else {
                attempts++;
                field.clear();
                if (attempts >= MAX_ATTEMPTS) {
                    error.setText("Too many attempts. Exiting.");
                    PauseTransition p = new PauseTransition(Duration.millis(1500));
                    p.setOnFinished(e -> { stage.close(); Platform.exit(); System.exit(0); });
                    p.play();
                } else {
                    error.setText("Wrong PIN. Attempts left: " + (MAX_ATTEMPTS - attempts));
                }
            }
        };

        unlock.setOnAction(e -> tryUnlock.run());
        field.setOnAction(e -> tryUnlock.run());
        exit.setOnAction(e -> { stage.close(); Platform.exit(); System.exit(0); });

        Scene scene = new Scene(root, 340, 320);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        Platform.runLater(field::requestFocus);
        stage.showAndWait();
        return result[0];
    }
}
