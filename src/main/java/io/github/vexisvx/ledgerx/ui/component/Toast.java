package io.github.vexisvx.ledgerx.ui.component;

import io.github.vexisvx.ledgerx.ui.anim.Animations;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class Toast {

    public enum Type { SUCCESS, ERROR, INFO }

    public static void show(Window owner, String message, Type type) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-family: 'Vazirmatn','Segoe UI',sans-serif;");

        HBox box = new HBox(10, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        String color = switch (type) {
            case SUCCESS -> "rgba(0, 212, 170, 0.95)";
            case ERROR -> "rgba(255, 107, 107, 0.95)";
            default -> "rgba(90, 140, 255, 0.95)";
        };
        box.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 20 12 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.3, 0, 6);"
        );
        box.setOpacity(0);
        box.setTranslateY(20);

        popup.getContent().add(box);

        double x = owner.getX() + owner.getWidth() / 2 - 150;
        double y = owner.getY() + owner.getHeight() - 100;
        popup.show(owner, x, y);

        FadeTransition ft = new FadeTransition(Duration.millis(250), box);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), box);
        tt.setFromY(20); tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition in = new ParallelTransition(ft, tt);
        in.play();

        PauseTransition pause = new PauseTransition(Duration.millis(2500));
        pause.setOnFinished(e -> {
            FadeTransition out = new FadeTransition(Duration.millis(300), box);
            out.setFromValue(1);
            out.setToValue(0);
            out.setOnFinished(ee -> popup.hide());
            out.play();
        });
        pause.play();
    }

    public static void success(Window owner, String msg) { show(owner, msg, Type.SUCCESS); }
    public static void error(Window owner, String msg) { show(owner, msg, Type.ERROR); }
    public static void info(Window owner, String msg) { show(owner, msg, Type.INFO); }
}
