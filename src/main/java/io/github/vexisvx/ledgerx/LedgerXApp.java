package io.github.vexisvx.ledgerx;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.dao.DatabaseInitializer;
import io.github.vexisvx.ledgerx.service.RecurringService;
import io.github.vexisvx.ledgerx.ui.component.PinDialog;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.ui.theme.FontLoader;
import io.github.vexisvx.ledgerx.ui.theme.ThemeManager;
import io.github.vexisvx.ledgerx.util.AppLogger;
import io.github.vexisvx.ledgerx.util.DatabaseManager;
import io.github.vexisvx.ledgerx.util.PinService;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LedgerXApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FontLoader.load();
        DatabaseInitializer.initialize();

        // Process any due recurring transactions
        try {
            int n = new RecurringService().processDue();
            if (n > 0) {
                AppLogger.info("Processed " + n + " recurring transaction(s)");
            }
        } catch (Exception e) {
            AppLogger.error("Recurring processing failed", e);
        }

        // PIN check first
        if (PinService.isEnabled()) {
            boolean ok = PinDialog.show();
            if (!ok) {
                Platform.exit();
                return;
            }
        }

        Stage splash = createSplash();
        splash.show();
        PauseTransition pause = new PauseTransition(Duration.millis(2000));
        pause.setOnFinished(e -> {
            splash.close();
            showMain(primaryStage);
        });
        pause.play();
    }

    private void showMain(Stage stage) {
        try {
            AppLogger.info("LedgerX started - v" + AppConfig.APP_VERSION);

            FXMLLoader loader = new FXMLLoader(
                LedgerXApp.class.getResource("/io/github/vexisvx/ledgerx/view/main.fxml"));
            loader.setResources(I18n.getBundle());
            Scene scene = new Scene(loader.load(), 1280, 800);
            ThemeManager.apply(scene);
            scene.getRoot().setNodeOrientation(
                I18n.isRtl() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

            stage.setTitle("LedgerX - " + I18n.t("app.subtitle"));
            stage.setScene(scene);
            stage.setMinWidth(1050);
            stage.setMinHeight(700);

            ThemeManager.registerWindow(stage);

            scene.getRoot().setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(500), scene.getRoot());
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.show();
        } catch (Exception ex) {
            AppLogger.error("Failed to show main window", ex);
            ex.printStackTrace();
            Platform.exit();
        }
    }

    private Stage createSplash() {
        Stage splash = new Stage(StageStyle.TRANSPARENT);
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(500, 340);
        box.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #0b1220, #111a28);" +
            "-fx-background-radius: 24;" +
            "-fx-border-radius: 24;" +
            "-fx-border-color: rgba(0,212,170,0.35);" +
            "-fx-border-width: 1.5;"
        );

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00d4aa", 0.4));
        glow.setRadius(60);
        glow.setSpread(0.25);
        box.setEffect(glow);

        StackPane logo = new StackPane();
        logo.setPrefSize(110, 110);
        logo.setMaxSize(110, 110);

        Region logoBg = new Region();
        logoBg.setPrefSize(110, 110);
        logoBg.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #00d4aa, #0092ff);" +
            "-fx-background-radius: 28;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,212,170,0.7), 30, 0.4, 0, 8);"
        );

        Label lx = new Label("LX");
        lx.setTextFill(Color.WHITE);
        lx.setFont(Font.font(FontLoader.getBaseFamily(), FontWeight.BLACK, 44));

        logo.getChildren().addAll(logoBg, lx);

        Label name = new Label("LedgerX");
        name.setTextFill(Color.web("#e8f0f8"));
        name.setFont(Font.font(FontLoader.getBaseFamily(), FontWeight.EXTRA_BOLD, 40));

        Label subtitle = new Label(I18n.t("app.subtitle"));
        subtitle.setTextFill(Color.web("#7a8ba0"));
        subtitle.setFont(Font.font(FontLoader.getBaseFamily(), 13));

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(220);
        divider.setStyle("-fx-background-color: linear-gradient(to right, transparent, #00d4aa55, transparent);");

        Label by = new Label(I18n.t("splash.by"));
        by.setTextFill(Color.web("#8899aa"));
        by.setFont(Font.font(FontLoader.getBaseFamily(), 12));

        Label loading = new Label(I18n.t("app.loading"));
        loading.setTextFill(Color.web("#556677"));
        loading.setFont(Font.font(FontLoader.getBaseFamily(), 10));

        box.getChildren().addAll(logo, name, subtitle, divider, by, loading);
        VBox.setMargin(by, new javafx.geometry.Insets(10, 0, 0, 0));
        root.getChildren().add(box);

        box.setOpacity(0);
        box.setScaleX(0.9);
        box.setScaleY(0.9);

        FadeTransition ft = new FadeTransition(Duration.millis(600), box);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.millis(700), box);
        st.setToX(1);
        st.setToY(1);
        st.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, st).play();

        ScaleTransition pulse = new ScaleTransition(Duration.millis(1200), logo);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.06);
        pulse.setToY(1.06);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        Timeline dots = new Timeline(
            new KeyFrame(Duration.ZERO, e -> loading.setText(".  ")),
            new KeyFrame(Duration.millis(400), e -> loading.setText("..  ")),
            new KeyFrame(Duration.millis(800), e -> loading.setText("... "))
        );
        dots.setCycleCount(Animation.INDEFINITE);
        dots.play();

        Scene scene = new Scene(root, 500, 340);
        scene.setFill(Color.TRANSPARENT);
        splash.setScene(scene);
        return splash;
    }

    @Override
    public void stop() {
        DatabaseManager.close();
        AppLogger.info("LedgerX closed");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
