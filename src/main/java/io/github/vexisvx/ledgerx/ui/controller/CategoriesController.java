package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

public class CategoriesController {
    @FXML private Label lblTitle, lblSubtitle, lblTotal, lblEmptyTitle, lblEmptyDesc;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button btnAdd;
    @FXML private ScrollPane catScroll;
    @FXML private TilePane catGrid;
    @FXML private VBox emptyState;

    private final CategoryService categoryService = new CategoryService();
    private final TransactionDao txDao = new TransactionDao();

    @FXML
    public void initialize() {
        if (lblTitle != null) lblTitle.setText(I18n.t("cat.title"));
        if (btnAdd != null) btnAdd.setText(I18n.t("cat.new"));

        // اطمینان از وجود دسته‌بندی‌های پیش‌فرض
        ensureCategories();

        if (typeFilter != null) {
            typeFilter.setItems(FXCollections.observableArrayList(
                I18n.t("tx.filterAll"), I18n.t("tx.filterIncome"), I18n.t("tx.filterExpense")));
            typeFilter.getSelectionModel().selectFirst();
            typeFilter.valueProperty().addListener((o, a, b) -> render());
        }
        render();
    }

    private void ensureCategories() {
        if (categoryService.getAll().isEmpty()) {
            seedDefaultCategories();
        }
    }

    private void seedDefaultCategories() {
        try {
            Object[][] seeds = {
                {"Food", "Food", "EXPENSE"},
                {"Transport", "Transport", "EXPENSE"},
                {"Housing", "Housing", "EXPENSE"},
                {"Shopping", "Shopping", "EXPENSE"},
                {"Health", "Health", "EXPENSE"},
                {"Bills", "Bills", "EXPENSE"},
                {"Entertainment", "Entertainment", "EXPENSE"},
                {"Salary", "Salary", "INCOME"}
            };
            for (Object[] seed : seeds) {
                Category c = new Category();
                c.setName((String) seed[0]);
                c.setNameEn((String) seed[1]);
                c.setType(TransactionType.valueOf((String) seed[2]));
                c.setIcon((String) seed[0]);
                categoryService.create(c);
            }
            System.out.println(">>> Seeded " + seeds.length + " default categories");
        } catch (Exception e) {
            System.err.println(">>> Failed to seed categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void render() {
        catGrid.getChildren().clear();
        List<Category> all = categoryService.getAll();

        if (all.isEmpty()) {
            seedDefaultCategories();
            all = categoryService.getAll();
        }

        int idx = typeFilter != null ? typeFilter.getSelectionModel().getSelectedIndex() : 0;
        if (idx == 1) {
            all = all.stream().filter(c -> c.getType() == TransactionType.INCOME).toList();
        } else if (idx == 2) {
            all = all.stream().filter(c -> c.getType() == TransactionType.EXPENSE).toList();
        }

        if (all.isEmpty()) {
            if (emptyState != null) {
                emptyState.setVisible(true);
                emptyState.setManaged(true);
            }
            if (catScroll != null) {
                catScroll.setVisible(false);
                catScroll.setManaged(false);
            }
            return;
        }

        if (emptyState != null) {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
        }
        if (catScroll != null) {
            catScroll.setVisible(true);
            catScroll.setManaged(true);
        }

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

        if (lblTotal != null) {
            lblTotal.setText(all.size() + " categories · " + Formatter.money(grandTotal) + " total");
        }

        for (Category c : all) {
            BigDecimal total = totals.getOrDefault(c.getId(), BigDecimal.ZERO);
            int count = counts.getOrDefault(c.getId(), 0);
            double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                ? total.divide(grandTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;
            catGrid.getChildren().add(buildCategoryCard(c, total, count, pct));
        }
    }

    private VBox buildCategoryCard(Category c, BigDecimal total, int count, double pct) {
        VBox card = new VBox();
        card.getStyleClass().add("cat-card");

        String color = categoryColor(c.getName());
        Circle circle = new Circle(24);
        circle.setFill(Color.web(color, 0.20));
        circle.setStroke(Color.web(color, 0.60));
        circle.setStrokeWidth(1.2);

        String firstLetter = "?";
        if (c.getName() != null && !c.getName().isEmpty()) {
            firstLetter = c.getName().substring(0, 1).toUpperCase();
        }

        Label letter = new Label(firstLetter);
        letter.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: 900; -fx-font-size: 18px;");

        StackPane badge = new StackPane(circle, letter);
        badge.setPrefSize(48, 48);
        badge.setMaxSize(48, 48);
        badge.getStyleClass().add("cat-card-icon");

        Label name = new Label(c.displayName());
        name.getStyleClass().add("cat-card-name");

        Label type = new Label(c.getType() == TransactionType.INCOME
            ? I18n.t("tx.filterIncome")
            : I18n.t("tx.filterExpense"));
        type.getStyleClass().add("cat-card-type");

        Label totalLbl = new Label(Formatter.money(total));
        totalLbl.getStyleClass().add("cat-card-total");

        Label countLbl = new Label(count + " " + I18n.t("cat.transactions"));
        countLbl.getStyleClass().add("cat-card-count");

        ProgressBar bar = new ProgressBar(Math.min(pct / 100.0, 1.0));
        bar.getStyleClass().add("cat-card-progress");
        bar.setMaxWidth(Double.MAX_VALUE);

        Label pctLbl = new Label(String.format("%.0f%%", pct));
        pctLbl.getStyleClass().add("cat-card-percent");

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(pctLbl, bar);
        HBox.setHgrow(bar, Priority.ALWAYS);

        HBox actions = new HBox(6);
        actions.getStyleClass().add("cat-card-actions");

        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("action-btn-text");
        editBtn.setOnAction(e -> showCategoryDialog(c));

        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("action-btn-text", "danger");
        if (isDefaultCategory(c)) {
            delBtn.setDisable(true);
            delBtn.setOpacity(0.4);
            delBtn.setTooltip(new Tooltip(I18n.t("cat.cannotDelete")));
        } else {
            delBtn.setOnAction(e -> deleteCategory(c));
        }

        actions.getChildren().addAll(editBtn, delBtn);

        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        nameRow.getChildren().addAll(name, spacer, actions);

        card.getChildren().addAll(badge, nameRow, type, totalLbl, countLbl, footer);
        return card;
    }

    private String categoryColor(String name) {
        if (name == null) return "#6a7a8a";
        String n = name.toLowerCase();
        if (n.contains("food") || n.contains("خوراک")) return "#f59e0b";
        if (n.contains("transport") || n.contains("حمل")) return "#3b82f6";
        if (n.contains("housing") || n.contains("مسکن")) return "#a855f7";
        if (n.contains("salary") || n.contains("حقوق")) return "#10b981";
        if (n.contains("shop") || n.contains("خرید")) return "#ec4899";
        if (n.contains("health") || n.contains("سلامت")) return "#ef4444";
        if (n.contains("bill") || n.contains("قبض")) return "#eab308";
        if (n.contains("entertain") || n.contains("سرگرمی")) return "#06b6d4";
        String[] colors = {"#f59e0b", "#3b82f6", "#a855f7", "#10b981", "#ec4899", "#ef4444", "#eab308", "#06b6d4"};
        return colors[Math.abs(name.hashCode()) % colors.length];
    }

    @FXML
    private void add() {
        showCategoryDialog(null);
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
        InputFilter.maxLength(nameField, 50);

        Label typeLbl = new Label(I18n.t("cat.type"));
        typeLbl.getStyleClass().add("form-label");
        ComboBox<TransactionType> typeCombo = new ComboBox<>(
            FXCollections.observableArrayList(TransactionType.values()));
        typeCombo.setValue(existing != null ? existing.getType() : TransactionType.EXPENSE);
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(nameLbl, nameField, typeLbl, typeCombo);

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getStylesheets().addAll(
            getClass().getResource("/css/base.css").toExternalForm(),
            getClass().getResource("/css/theme-" + io.github.vexisvx.ledgerx.config.AppConfig.getTheme() + ".css").toExternalForm());

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                try {
                    Validator.validateName(nameField.getText());
                    Category c = existing != null ? existing : new Category();
                    c.setName(nameField.getText().trim());
                    c.setNameEn(nameField.getText().trim());
                    c.setType(typeCombo.getValue());
                    c.setIcon(categoryColor(c.getName()));
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
                if (existing == null) {
                    categoryService.create(c);
                } else {
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
        // چک نهایی: پیش‌فرض حذف نشه
        if (isDefaultCategory(c)) {
            Toast.error(getWindow(), I18n.t("cat.cannotDelete"));
            return;
        }

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

    
    private boolean isDefaultCategory(Category c) {
        if (c == null || c.getName() == null) return false;
        String n = c.getName().toLowerCase().trim();
        // نام‌های پیش‌فرض که DatabaseInitializer seed می‌کنه
        return n.equals("salary") || n.equals("food") || n.equals("transport")
            || n.equals("housing") || n.equals("shopping") || n.equals("health")
            || n.equals("bills") || n.equals("entertainment")
            || n.equals("حقوق") || n.equals("خوراک") || n.equals("حمل‌ونقل")
            || n.equals("مسکن") || n.equals("خرید") || n.equals("سلامت")
            || n.equals("قبض") || n.equals("سرگرمی");
    }

private javafx.stage.Window getWindow() {
        return catGrid.getScene() != null ? catGrid.getScene().getWindow() : null;
    }
}
