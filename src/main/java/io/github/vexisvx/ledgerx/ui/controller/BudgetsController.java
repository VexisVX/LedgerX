package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.BudgetService;
import io.github.vexisvx.ledgerx.service.CategoryService;
import io.github.vexisvx.ledgerx.ui.component.InputFilter;
import io.github.vexisvx.ledgerx.ui.component.Toast;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.util.Formatter;
import io.github.vexisvx.ledgerx.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetsController {

    @FXML private Label lblTitle, lblSubtitle, lblEmptyTitle, lblEmptyDesc;
    @FXML private Button btnNew;
    @FXML private TilePane budgetGrid;
    @FXML private VBox emptyState;

    private final BudgetService budgetService = new BudgetService();
    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        if (lblTitle != null) lblTitle.setText(I18n.t("budget.title"));
        if (btnNew != null) btnNew.setText(I18n.t("budget.new"));

        ensureCategories();
        render();
    }

    private void ensureCategories() {
        if (categoryService.getByType(TransactionType.EXPENSE).isEmpty()) {
            seedCategories();
        }
    }

    private void seedCategories() {
        try {
            String[] names = {"Food", "Transport", "Housing", "Shopping", "Health", "Bills", "Entertainment"};
            for (String name : names) {
                Category c = new Category();
                c.setName(name);
                c.setNameEn(name);
                c.setType(TransactionType.EXPENSE);
                c.setIcon(name);
                categoryService.create(c);
            }
            System.out.println(">>> Seeded " + names.length + " expense categories");
        } catch (Exception e) {
            System.err.println(">>> Seed failed: " + e.getMessage());
        }
    }

    private void render() {
        budgetGrid.getChildren().clear();
        LocalDate now = LocalDate.now();
        List<Budget> budgets = budgetService.getAllWithSpending(now.getYear(), now.getMonthValue());

        if (budgets.isEmpty()) {
            if (emptyState != null) {
                emptyState.setVisible(true);
                emptyState.setManaged(true);
            }
            budgetGrid.setVisible(false);
            budgetGrid.setManaged(false);
            return;
        }
        if (emptyState != null) {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
        }
        budgetGrid.setVisible(true);
        budgetGrid.setManaged(true);

        for (Budget b : budgets) {
            budgetGrid.getChildren().add(buildBudgetCard(b));
        }
    }

    private VBox buildBudgetCard(Budget b) {
        VBox card = new VBox();
        card.getStyleClass().add("budget-card");

        double pct = b.getUsagePercent();
        if (pct >= 100) card.getStyleClass().add("danger");
        else if (pct >= 80) card.getStyleClass().add("warn");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(b.getCategoryName());
        name.getStyleClass().add("budget-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("action-btn-text");
        editBtn.setOnAction(e -> showEditDialog(b));

        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("action-btn-text", "danger");
        delBtn.setOnAction(e -> deleteBudget(b));

        actions.getChildren().addAll(editBtn, delBtn);

        header.getChildren().addAll(name, actions);

        HBox amounts = new HBox(8);
        amounts.setAlignment(Pos.BASELINE_LEFT);
        Label spent = new Label(Formatter.money(b.getSpent()));
        spent.getStyleClass().add("budget-spent");
        Label limit = new Label("/ " + Formatter.money(b.getMonthlyLimit()));
        limit.getStyleClass().add("budget-limit");
        amounts.getChildren().addAll(spent, limit);

        ProgressBar bar = new ProgressBar(Math.min(pct / 100.0, 1.0));
        bar.getStyleClass().add("budget-progress");
        bar.setMaxWidth(Double.MAX_VALUE);
        if (pct >= 100) bar.getStyleClass().add("danger");
        else if (pct >= 80) bar.getStyleClass().add("warn");

        HBox meta = new HBox(8);
        Label percentLbl = new Label(String.format("%.0f%%", pct));
        percentLbl.getStyleClass().add("budget-percent");
        String color = pct >= 100 ? "#ff6b6b" : pct >= 80 ? "#f5a623" : "#00d4aa";
        percentLbl.setStyle("-fx-text-fill: " + color + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        BigDecimal remaining = b.getMonthlyLimit().subtract(b.getSpent());
        String remainText = remaining.compareTo(BigDecimal.ZERO) >= 0
            ? I18n.t("budget.remainingLabel") + ": " + Formatter.money(remaining)
            : I18n.t("budget.over") + ": " + Formatter.money(remaining.abs());
        Label remainingLbl = new Label(remainText);
        remainingLbl.getStyleClass().add("budget-remaining");

        meta.getChildren().addAll(percentLbl, spacer, remainingLbl);

        card.getChildren().addAll(header, amounts, bar, meta);
        return card;
    }

    @FXML
    private void add() {
        showEditDialog(null);
    }

    private void showEditDialog(Budget existing) {
        ensureCategories();

        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? I18n.t("budget.new") : I18n.t("budget.edit"));
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType(I18n.t("form.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: -fx-bg;");
        root.setPrefWidth(380);

        Label catLbl = new Label(I18n.t("budget.category"));
        catLbl.getStyleClass().add("form-label");
        ComboBox<Category> catCombo = new ComboBox<>(
            FXCollections.observableArrayList(categoryService.getByType(TransactionType.EXPENSE)));
        catCombo.setMaxWidth(Double.MAX_VALUE);
        if (existing != null) {
            for (Category c : catCombo.getItems()) {
                if (c.getId().equals(existing.getCategoryId())) {
                    catCombo.setValue(c);
                    break;
                }
            }
            catCombo.setDisable(true);
        }

        Label limitLbl = new Label(I18n.t("budget.limit"));
        limitLbl.getStyleClass().add("form-label");
        TextField limitField = new TextField(existing != null
            ? existing.getMonthlyLimit().toPlainString() : "");
        limitField.setPromptText("0");
        InputFilter.amountField(limitField);

        root.getChildren().addAll(catLbl, catCombo, limitLbl, limitField);

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getStylesheets().addAll(
            getClass().getResource("/css/base.css").toExternalForm(),
            getClass().getResource("/css/theme-" + io.github.vexisvx.ledgerx.config.AppConfig.getTheme() + ".css").toExternalForm());

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                try {
                    Category cat = catCombo.getValue();
                    if (cat == null) throw new IllegalArgumentException("Select a category");
                    BigDecimal limit = Validator.parseAmount(limitField.getText());
                    Validator.validateBudgetLimit(limit);

                    Budget b = new Budget();
                    b.setId(existing != null ? existing.getId() : null);
                    b.setCategoryId(cat.getId());
                    b.setCategoryName(cat.displayName());
                    b.setMonthlyLimit(limit);
                    return b;
                } catch (IllegalArgumentException e) {
                    Toast.error(getWindow(), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(b -> {
            try {
                budgetService.setBudget(b.getCategoryId(), b.getMonthlyLimit());
                render();
                Toast.success(getWindow(), I18n.t("budget.saved"));
            } catch (Exception e) {
                Toast.error(getWindow(), e.getMessage());
            }
        });
    }

    private void deleteBudget(Budget b) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            String.format(I18n.t("budget.confirmDelete"), b.getCategoryName()));
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                budgetService.delete(b.getId());
                render();
                Toast.success(getWindow(), I18n.t("budget.deleted"));
            }
        });
    }

    private javafx.stage.Window getWindow() {
        return budgetGrid.getScene() != null ? budgetGrid.getScene().getWindow() : null;
    }
}
