package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CategoriesController {
    @FXML private Label lblHeaderIcon, lblTitle, lblSubtitle, lblTotal;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button btnAdd;
    @FXML private TilePane catGrid;

    private final CategoryService categoryService = new CategoryService();
    private final TransactionDao txDao = new TransactionDao();

    @FXML
    public void initialize() {
        lblHeaderIcon.setText("\uD83D\uDCC1");
        lblTitle.setText(I18n.t("cat.title"));
        btnAdd.setText("+ " + I18n.t("cat.new"));

        typeFilter.setItems(FXCollections.observableArrayList(
            I18n.t("tx.filterAll"), I18n.t("tx.filterIncome"), I18n.t("tx.filterExpense")));
        typeFilter.getSelectionModel().selectFirst();
        typeFilter.valueProperty().addListener((o, a, b) -> render());
        render();
    }

    private void render() {
        catGrid.getChildren().clear();
        List<Category> all = categoryService.getAll();

        int idx = typeFilter.getSelectionModel().getSelectedIndex();
        if (idx == 1) all = all.stream().filter(c -> c.getType() == TransactionType.INCOME).collect(Collectors.toList());
        else if (idx == 2) all = all.stream().filter(c -> c.getType() == TransactionType.EXPENSE).collect(Collectors.toList());

        // Compute stats per category
        LocalDate now = LocalDate.now();
        int year = now.getYear(), month = now.getMonthValue();
        List<Transaction> txs = txDao.findAll();

        Map<Long, BigDecimal> totals = new HashMap<>();
        Map<Long, Integer> counts = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Transaction t : txs) {
            if (t.getType() != TransactionType.EXPENSE) continue;
            if (t.getDate().getYear() != year || t.getDate().getMonthValue() != month) continue;
            if (t.getCategoryId() == null) continue;
            totals.merge(t.getCategoryId(), t.getAmount(), BigDecimal::add);
            counts.merge(t.getCategoryId(), 1, Integer::sum);
            grandTotal = grandTotal.add(t.getAmount());
        }

        lblTotal.setText(String.format("%d categories · %s total this month",
            all.size(), Formatter.money(grandTotal)));

        for (Category c : all) {
            BigDecimal total = totals.getOrDefault(c.getId(), BigDecimal.ZERO);
            int count = counts.getOrDefault(c.getId(), 0);
            double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                ? total.divide(grandTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
            catGrid.getChildren().add(buildCategoryCard(c, total, count, pct));
        }
    }

    private VBox buildCategoryCard(Category c, BigDecimal total, int count, double pct) {
        VBox card = new VBox();
        card.getStyleClass().add("cat-card");

        Label icon = new Label(c.getIcon() != null && !c.getIcon().isBlank() ? c.getIcon() : firstLetter(c.getName()));
        icon.getStyleClass().add("cat-card-icon");
        icon.setStyle("-fx-text-fill: -fx-accent; -fx-font-weight: 900; -fx-font-size: 24px;");

        Label name = new Label(c.displayName());
        name.getStyleClass().add("cat-card-name");

        Label type = new Label(c.getType() == TransactionType.INCOME
            ? I18n.t("tx.filterIncome") : I18n.t("tx.filterExpense"));
        type.getStyleClass().add("cat-card-type");

        Label totalLbl = new Label(Formatter.money(total));
        totalLbl.getStyleClass().add("cat-card-total");

        Label countLbl = new Label(count + " " + I18n.t("cat.transactions"));
        countLbl.getStyleClass().add("cat-card-count");

        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.getStyleClass().add("cat-card-progress");
        bar.setMaxWidth(Double.MAX_VALUE);

        Label pctLbl = new Label(String.format("%.0f%%", pct));
        pctLbl.getStyleClass().add("cat-card-percent");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setSpacing(8);
        footer.getChildren().addAll(pctLbl, bar);
        HBox.setHgrow(bar, Priority.ALWAYS);

        HBox actions = new HBox();
        actions.getStyleClass().add("cat-card-actions");
        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("action-btn-text");
        editBtn.setTooltip(new Tooltip(I18n.t("cat.edit")));
        editBtn.setOnAction(e -> editCategory(c));

        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("action-btn-text", "danger");
        delBtn.setTooltip(new Tooltip(I18n.t("cat.delete")));
        delBtn.setOnAction(e -> deleteCategory(c));

        actions.getChildren().addAll(editBtn, delBtn);

        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getChildren().addAll(name, new Region(), actions);
        HBox.setHgrow(nameRow.getChildren().get(1), Priority.ALWAYS);

        card.getChildren().addAll(icon, nameRow, type, totalLbl, countLbl, footer);
        return card;
    }

    @FXML private void add() {
        showCategoryDialog(null);
    }

    private void editCategory(Category c) {
        showCategoryDialog(c);
    }

    private void showCategoryDialog(Category existing) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? I18n.t("cat.new") : I18n.t("cat.edit"));
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType(I18n.t("form.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: -fx-bg;");
        root.setPrefWidth(360);

        Label nameLbl = new Label(I18n.t("cat.name"));
        nameLbl.getStyleClass().add("form-label");
        TextField nameField = new TextField(existing != null ? existing.getName() : "");

        Label iconLbl = new Label(I18n.t("cat.icon"));
        iconLbl.getStyleClass().add("form-label");
        ComboBox<String> iconCombo = new ComboBox<>(FXCollections.observableArrayList(Icons.CATEGORY_ICONS));
        iconCombo.setValue(existing != null && existing.getIcon() != null ? existing.getIcon() : "\uD83D\uDCC1");

        Label typeLbl = new Label(I18n.t("cat.type"));
        typeLbl.getStyleClass().add("form-label");
        ComboBox<TransactionType> typeCombo = new ComboBox<>(
            FXCollections.observableArrayList(TransactionType.values()));
        typeCombo.setValue(existing != null ? existing.getType() : TransactionType.EXPENSE);

        root.getChildren().addAll(nameLbl, nameField, iconLbl, iconCombo, typeLbl, typeCombo);

        dialog.getDialogPane().setContent(root);
        if (dialog.getDialogPane().getScene() != null) {
            ThemeManager.apply(dialog.getDialogPane().getScene());
        }

        // Apply input filters
        InputFilter.maxLength(nameField, 50);

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                try {
                    Validator.validateName(nameField.getText());
                    Category c = existing != null ? existing : new Category();
                    c.setName(nameField.getText().trim());
                    c.setNameEn(nameField.getText().trim());
                    c.setIcon(iconCombo.getValue());
                    c.setType(typeCombo.getValue());
                    return c;
                } catch (IllegalArgumentException e) {
                    Toast.error(getWindow(), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            try {
                if (existing == null) categoryService.create(c);
                else {
                    // delete then insert (simple) — or update method
                    categoryService.delete(existing.getId());
                    c.setId(null);
                    categoryService.create(c);
                }
                render();
                Toast.success(getWindow(), I18n.t("cat.saved"));
            } catch (Exception e) {
                Toast.error(getWindow(), e.getMessage());
            }
        });
    }

    private void deleteCategory(Category c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            String.format(I18n.t("cat.confirmDelete"), c.displayName()));
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                categoryService.delete(c.getId());
                render();
                Toast.success(getWindow(), I18n.t("cat.deleted"));
            }
        });
    }

    private String firstLetter(String name) {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1).toUpperCase();
    }

    private javafx.stage.Window getWindow() {
        return catGrid.getScene() != null ? catGrid.getScene().getWindow() : null;
    }
}
