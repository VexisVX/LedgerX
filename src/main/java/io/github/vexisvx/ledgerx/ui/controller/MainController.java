package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.service.BudgetService;
import io.github.vexisvx.ledgerx.service.RecurringService;
import io.github.vexisvx.ledgerx.ui.anim.Animations;
import io.github.vexisvx.ledgerx.ui.component.Icons;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard, btnTransactions, btnCategories, btnBudgets, btnGoals, btnRecurring, btnSettings, btnAbout;
    @FXML private Label lblBrandFooter;
    @FXML private Label lblBudgetsBadge;
    @FXML private Label lblRecurringBadge;

    private Button activeBtn;

    @FXML
    public void initialize() {
        activeBtn = btnDashboard;
        setNav(btnDashboard, Icons.dashboard(), "nav.dashboard");
        setNav(btnTransactions, Icons.transactions(), "nav.transactions");
        setNav(btnCategories, Icons.categories(), "nav.categories");
        setNav(btnBudgets, Icons.budgets(), "nav.budgets");
        setNav(btnGoals, Icons.goals(), "nav.goals");
        setNav(btnRecurring, Icons.recurring(), "nav.recurring");
        setNav(btnSettings, Icons.settings(), "nav.settings");
        setNav(btnAbout, Icons.about(), "nav.about");
        if (lblBrandFooter != null) lblBrandFooter.setText("A " + AppConfig.BRAND + " Project");

        activeBtn.getStyleClass().add("nav-button-active");
        refreshBadges();
        Platform.runLater(this::showDashboard);
    }

        private void refreshBadges() {
        try {
            int overbudget = new BudgetService().getOverbudgetCount();
            int due = new RecurringService().getDueCount();

            setBadge(lblBudgetsBadge, overbudget, "budget");
            setBadge(lblRecurringBadge, due, "rec");
        } catch (Exception ignored) {}
    }

    private void setBadge(Label badge, int count, String type) {
        if (badge == null) return;
        if (count > 0) {
            badge.setText(count > 99 ? "99+" : String.valueOf(count));
            badge.setVisible(true);
            badge.setManaged(true);
            badge.getStyleClass().removeAll("badge-budget", "badge-rec");
            badge.getStyleClass().add("budget".equals(type) ? "badge-budget" : "badge-rec");
        } else {
            badge.setVisible(false);
            badge.setManaged(false);
        }
    }

    /** Public: called after transactions/budgets/recurring change. */
    public void onDataChanged() {
        refreshBadges();
    }

    private void setNav(Button b, String icon, String key) {
        b.setText(icon + "   " + I18n.t(key));
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("nav-button-active");
        activeBtn = btn;
        if (!btn.getStyleClass().contains("nav-button-active"))
            btn.getStyleClass().add("nav-button-active");
        Animations.pulse(btn);
    }

    @FXML private void showDashboard()    { setActive(btnDashboard);    loadView("dashboard.fxml"); }
    @FXML private void showTransactions() { setActive(btnTransactions); loadView("transactions.fxml"); }
    @FXML private void showCategories()   { setActive(btnCategories);   loadView("categories.fxml"); }
    @FXML private void showBudgets()      { setActive(btnBudgets);      loadView("budgets.fxml"); }
    @FXML private void showGoals()        { setActive(btnGoals);        loadView("goals.fxml"); }
    @FXML private void showRecurring()    { setActive(btnRecurring);    loadView("recurring.fxml"); }
    @FXML private void showSettings()     { setActive(btnSettings);     loadView("settings.fxml"); }
    @FXML private void showAbout()        { setActive(btnAbout);        loadView("about.fxml"); }

    private void loadView(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/io/github/vexisvx/ledgerx/view/" + name));
            loader.setResources(I18n.getBundle());
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            Animations.slideUpFade(view, 320);
            if (I18n.isRtl() && !view.getStyleClass().contains("rtl"))
                view.getStyleClass().add("rtl");
        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Error loading " + name + ": " + e.getMessage());
            err.setStyle("-fx-text-fill: #ff6b6b; -fx-padding: 20;");
            err.setWrapText(true);
            contentArea.getChildren().setAll(err);
        }
    }
}
