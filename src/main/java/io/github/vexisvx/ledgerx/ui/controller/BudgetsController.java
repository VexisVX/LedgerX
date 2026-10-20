package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.BudgetService;
import io.github.vexisvx.ledgerx.service.CategoryService;
import io.github.vexisvx.ledgerx.ui.component.Icons;
import io.github.vexisvx.ledgerx.ui.component.InputFilter;
import io.github.vexisvx.ledgerx.ui.component.Toast;
import io.github.vexisvx.ledgerx.ui.theme.ThemeManager;
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
        lblTitle.setText(I18n.t("budget.title"));
        btnNew.setText("+ " + I18n.t("budget.new"));
        lblEmptyTitle.setText(I18n.t("budget.empty.title"));
        lblEmptyDesc.setText(I18n.t("budget.empty.desc"));
        render();
    }

    private void render() {
        budgetGrid.getChildren().clear();
        LocalDate now = LocalDate.now();
        List<Budget> budgets = budgetService.getAllWithSpending(now.getYear(), now.getMonthValue());

        if (budgets.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            budgetGrid.setVisible(false);
            budgetGrid.setManaged(false);
            return;
        }
        emptyState.setVisible(false);
        emptyState.setManaged(false);
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

        // Header: icon + name + actions
        HBox header = new HBox();
        header.getStyleClass().add("budget-header");

        Label icon = new Label("[" + (b.getCategoryName() != null ? b.getCategoryName().substring(0, Math.min(2, b.getCategoryName().length())) : "?") + "]");
        icon.getStyleClass().add("budget-icon");
        icon.setStyle("-fx-text-fill: -fx-accent; -fx-font-weight: 800; -fx-font-size: 14px;");

        Label name = new Label(b.getCategoryName());
        name.getStyleClass().add("budget-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        HBox actions = new HBox();
        actions.getStyleClass().add("budget-actions");
        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("action-btn-text");
        editBtn.setTooltip(new Tooltip(I18n.t("budget.edit")));
        editBtn.setOnAction(e -> showEditDialog(b));

        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("action-btn-text", "danger");
        delBtn.setTooltip(new Tooltip(I18n.t("budget.delete")));
        delBtn.setOnAction(e -> deleteBudget(b));

        actions.getChildren().addAll(editBtn, delBtn);

        header.getChildren().addAll(icon, name, actions);

        // Amounts
        HBox amounts = new HBox();
        amounts.getStyleClass().add("budget-amount-row");
        Label spent = new Label(Formatter.money(b.getSpent()));
        spent.getStyleClass().add("budget-spent");
        Label limit = new Label("/ " + Formatter.money(b.getMonthlyLimit()));
        limit.getStyleClass().add("budget-limit");
        amounts.getChildren().addAll(spent, limit);

        // Progress
        ProgressBar bar = new ProgressBar(Math.min(pct / 100.0, 1.0));
        bar.getStyleClass().add("budget-progress");
        bar.setMaxWidth(Double.MAX_VALUE);

        // Meta: percent + remaining
        HBox meta = new HBox();
        meta.getStyleClass().add("budget-meta");
        Label percentLbl = new Label(String.format("%.0f%%", pct));
        percentLbl.getStyleClass().add("budget-percent");
        String color = pct >= 100 ? "#ff6b6b" : pct >= 80 ? "#f5a623" : "#00d4aa";
        percentLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        BigDecimal remaining = b.getMonthlyLimit().subtract(b.getSpent());
        String remainText;
        if (remaining.compareTo(BigDecimal.ZERO) >= 0) {
            remainText = I18n.t("budget.remainingLabel") + ": " + Formatter.money(remaining);
        } else {
            remainText = I18n.t("budget.over") + ": " + Formatter.money(remaining.abs());
        }
        Label remainingLbl = new Label(remainText);
        remainingLbl.getStyleClass().add("budget-remaining");

        meta.getChildren().addAll(percentLbl, spacer, remainingLbl);

        card.getChildren().addAll(header, amounts, bar, meta);
        return card;
    }

    private String categoryIcon(String name) {
        if (name == null) return "\uD83D\uDCC1";
        String n = name.toLowerCase();
        if (n.contains("food") || n.contains("خوراک")) return "\uD83C\uDF54";
        if (n.contains("transport") || n.contains("حمل")) return "\uD83D\uDE97";
        if (n.contains("housing") || n.contains("مسکن")) return "\uD83C\uDFE0";
        if (n.contains("salary") || n.contains("حقوق")) return "\uD83D\uDCB0";
        if (n.contains("shop") || n.contains("خرید")) return "\uD83D\uDECD";
        if (n.contains("health") || n.contains("سلامت")) return "\uD83C\uDFE5";
        if (n.contains("bill") || n.contains("قبض")) return "\uD83D\uDCC4";
        if (n.contains("entertain") || n.contains("سرگرمی")) return "\uD83C\uDFAE";
        return "\uD83D\uDCC1";
    }

    @FXML
    private void add() {
        showEditDialog(null);
    }

    private void showEditDialog(Budget existing) {
        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? I18n.t("budget.new") : I18n.t("budget.edit"));
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType(I18n.t("form.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: -fx-bg;");
        root.setPrefWidth(380);

        // Category picker
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
        }
        // Disable category change if editing (since UNIQUE constraint on category)
        if (existing != null) catCombo.setDisable(true);

        // Limit
        Label limitLbl = new Label(I18n.t("budget.limit"));
        limitLbl.getStyleClass().add("form-label");
        TextField limitField = new TextField(existing != null
            ? existing.getMonthlyLimit().toPlainString() : "");
        limitField.setPromptText("0");
        InputFilter.amountField(limitField);

        root.getChildren().addAll(catLbl, catCombo, limitLbl, limitField);

        dialog.getDialogPane().setContent(root);
        if (dialog.getDialogPane().getScene() != null) {
            ThemeManager.apply(dialog.getDialogPane().getScene());
        }

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
