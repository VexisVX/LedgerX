package io.github.vexisvx.ledgerx.ui.anim;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class Animations {

    public static void fadeIn(Node node, int ms) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public static void slideUpFade(Node node, int ms) {
        node.setOpacity(0);
        node.setTranslateY(24);
        ParallelTransition pt = new ParallelTransition();
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), node);
        tt.setFromY(24); tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        pt.getChildren().addAll(ft, tt);
        pt.play();
    }

    public static void slideInRight(Node node, int ms) {
        node.setTranslateX(40);
        node.setOpacity(0);
        ParallelTransition pt = new ParallelTransition();
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), node);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setToValue(1);
        pt.getChildren().addAll(tt, ft);
        pt.play();
    }

    public static void countUp(Label label, double from, double to, int ms, String suffix) {
        final long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long now) {
                double elapsedMs = (now - start) / 1_000_000.0;
                double p = Math.min(elapsedMs / ms, 1.0);
                double eased = 1 - Math.pow(1 - p, 3);
                double value = from + (to - from) * eased;
                label.setText(String.format("%,.2f", value) + suffix);
                if (p >= 1.0) stop();
            }
        };
        timer.start();
    }

    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), node);
        st.setFromX(1); st.setFromY(1);
        st.setToX(1.04); st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    public static void rotate(Node node, int ms) {
        RotateTransition rt = new RotateTransition(Duration.millis(ms), node);
        rt.setByAngle(360);
        rt.play();
    }
}
