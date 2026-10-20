package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.ui.anim.Animations;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private StackPane stackRoot;
    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private HBox brandBox;
    @FXML private VBox brandTextBox;
    @FXML private Label brandTag;
    @FXML private Button btnDashboard, btnTransactions, btnCategories, btnBudgets, btnGoals, btnRecurring, btnSettings, btnAbout;
    @FXML private Button fabButton;
    @FXML private Label lblBrandFooter;

    private Button activeBtn;
    private boolean compact;

    @FXML
    public void initialize() {
        compact = AppConfig.isCompactMode();
        activeBtn = btnDashboard;
        applyCompactLayout();
        refreshNav();
        if (lblBrandFooter != null) lblBrandFooter.setText("A " + AppConfig.BRAND + " Project");
        activeBtn.getStyleClass().add("nav-button-active");

        setupFab();

        Platform.runLater(this::showDashboard);
    }

    private void setupFab() {
        if (fabButton == null) {
            System.err.println("!!! FAB is NULL");
            return;
        }

        boolean show = AppConfig.isShowQuickAdd();
        System.out.println(">>> FAB setup: show=" + show);

        fabButton.setVisible(show);
        fabButton.setManaged(show);
        fabButton.setOpacity(1.0);
        fabButton.setDisable(false);

        // Position
        int offset = AppConfig.getFabOffset();
        StackPane.setMargin(fabButton, new Insets(0, 30, offset, 0));
        StackPane.setAlignment(fabButton, Pos.BOTTOM_RIGHT);

        // Force CSS
        Platform.runLater(() -> {
            fabButton.applyCss();
            fabButton.layout();
        });
    }

    private void applyCompactLayout() {
        if (sidebar == null) return;
        double width = compact ? 80 : 230;
        sidebar.setPrefWidth(width);
        sidebar.setMinWidth(width);
        sidebar.setMaxWidth(width);
        if (brandTextBox != null) {
            brandTextBox.setVisible(!compact);
            brandTextBox.setManaged(!compact);
        }
        if (brandTag != null) {
            brandTag.setVisible(!compact);
            brandTag.setManaged(!compact);
        }
        if (lblBrandFooter != null) {
            lblBrandFooter.setVisible(!compact);
            lblBrandFooter.setManaged(!compact);
        }
    }

    private void refreshNav() {
        setNav(btnDashboard, "D", "nav.dashboard");
        setNav(btnTransactions, "T", "nav.transactions");
        setNav(btnCategories, "C", "nav.categories");
        setNav(btnBudgets, "B", "nav.budgets");
        setNav(btnGoals, "G", "nav.goals");
        setNav(btnRecurring, "R", "nav.recurring");
        setNav(btnSettings, "S", "nav.settings");
        setNav(btnAbout, "i", "nav.about");
    }

    private void setNav(Button b, String code, String key) {
        if (b == null) return;
        if (compact) {
            b.setText(code);
            b.setAlignment(Pos.CENTER);
            b.setStyle("-fx-alignment: CENTER; -fx-padding: 11 0; -fx-font-weight: 800; -fx-font-size: 13px;");
            b.setTooltip(new javafx.scene.control.Tooltip(I18n.t(key)));
        } else {
            b.setText(code + "   " + I18n.t(key));
            b.setAlignment(Pos.CENTER_LEFT);
            b.setStyle("-fx-alignment: CENTER_LEFT;");
            b.setTooltip(null);
        }
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("nav-button-active");
        activeBtn = btn;
        if (!btn.getStyleClass().contains("nav-button-active"))
            btn.getStyleClass().add("nav-button-active");
        Animations.pulse(btn);
    }

    @FXML public void showDashboard()    { setActive(btnDashboard);    loadView("dashboard.fxml"); }
    @FXML public void showTransactions() { setActive(btnTransactions); loadView("transactions.fxml"); }
    @FXML public void showCategories()   { setActive(btnCategories);   loadView("categories.fxml"); }
    @FXML public void showBudgets()      { setActive(btnBudgets);      loadView("budgets.fxml"); }
    @FXML public void showGoals()        { setActive(btnGoals);        loadView("goals.fxml"); }
    @FXML public void showRecurring()    { setActive(btnRecurring);    loadView("recurring.fxml"); }
    @FXML public void showSettings()     { setActive(btnSettings);     loadView("settings.fxml"); }
    @FXML public void showAbout()        { setActive(btnAbout);        loadView("about.fxml"); }

    @FXML
    public void quickAdd() {
        try {
            java.net.URL formUrl = getClass().getResource("/io/github/vexisvx/ledgerx/view/transaction-form.fxml");
            if (formUrl == null) {
                showError("transaction-form.fxml not found");
                return;
            }

            FXMLLoader loader = new FXMLLoader(formUrl);
            loader.setResources(I18n.getBundle());
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            java.net.URL baseCss = getClass().getResource("/css/base.css");
            java.net.URL themeCss = getClass().getResource("/css/theme-" + AppConfig.getTheme() + ".css");
            if (baseCss != null) scene.getStylesheets().add(baseCss.toExternalForm());
            if (themeCss != null) scene.getStylesheets().add(themeCss.toExternalForm());

            TransactionFormController ctrl = loader.getController();
            ctrl.setTransaction(null);

            javafx.stage.Stage st = new javafx.stage.Stage();
            st.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            st.setTitle(I18n.t("form.title.new"));
            st.setScene(scene);
            st.showAndWait();

            if (activeBtn == btnDashboard) loadView("dashboard.fxml");
            else if (activeBtn == btnTransactions) loadView("transactions.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void loadView(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/io/github/vexisvx/ledgerx/view/" + name));
            loader.setResources(I18n.getBundle());
            Node view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof DashboardController dc) dc.setMainController(this);

            // ONLY contentArea children changed, FAB stays in stackRoot
            contentArea.getChildren().setAll(view);
            Animations.slideUpFade(view, 320);

            // Ensure FAB is still visible & on top
            if (fabButton != null) {
                fabButton.toFront();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Error: " + e.getMessage());
            err.setStyle("-fx-text-fill: #ff6b6b; -fx-padding: 20;");
            contentArea.getChildren().setAll(err);
        }
    }
}
