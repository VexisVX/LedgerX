package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.CategoryService;
import io.github.vexisvx.ledgerx.service.TransactionService;
import io.github.vexisvx.ledgerx.ui.component.Toast;
import io.github.vexisvx.ledgerx.ui.theme.ThemeManager;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.util.CsvExporter;
import io.github.vexisvx.ledgerx.util.ExcelExporter;
import io.github.vexisvx.ledgerx.util.PdfExporter;
import io.github.vexisvx.ledgerx.util.Formatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsController {
    @FXML private Label lblHeaderIcon, lblTitle, lblSubtitle;
    @FXML private Label lblIncomeTile, lblExpenseTile, lblCountTile;
    @FXML private Label lblIncomeValue, lblExpenseValue, lblCountValue;
    @FXML private Label lblDateFrom, lblDateTo, lblMinAmount, lblMaxAmount, lblSort;
    @FXML private Label lblEmptyTitle, lblEmptyDesc;
    @FXML private TextField searchField, minAmountField, maxAmountField;
    @FXML private ComboBox<String> typeFilter, sortCombo;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private Button btnNew, btnClearFilters;
    @FXML private ToggleButton btnAdvanced;
    @FXML private HBox advancedPanel;
    @FXML private DatePicker dateFromPicker, dateToPicker;
    @FXML private MenuButton btnExport;
    @FXML private MenuItem miCsv, miExcel;
    @FXML private ScrollPane txScroll;
    @FXML private VBox txListContainer, emptyState;

    private final TransactionService service = new TransactionService();
    private final CategoryService categoryService = new CategoryService();
    private ObservableList<Transaction> masterData;

    @FXML
    public void initialize() {
        translate();
        setupFilter();
        masterData = FXCollections.observableArrayList(service.getAll());
        applyFilter();

        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        typeFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        categoryFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        dateFromPicker.valueProperty().addListener((o, a, b) -> applyFilter());
        dateToPicker.valueProperty().addListener((o, a, b) -> applyFilter());
        minAmountField.textProperty().addListener((o, a, b) -> applyFilter());
        maxAmountField.textProperty().addListener((o, a, b) -> applyFilter());
        sortCombo.valueProperty().addListener((o, a, b) -> applyFilter());

        btnAdvanced.selectedProperty().addListener((o, oldV, newV) -> {
            advancedPanel.setVisible(newV);
            advancedPanel.setManaged(newV);
        });
    }

    private void translate() {
        lblHeaderIcon.setText("\uD83D\uDCB3");
        lblTitle.setText(I18n.t("tx.title"));
        lblSubtitle.setText(I18n.t("tx.subtitle"));
        lblIncomeTile.setText(I18n.t("dashboard.income"));
        lblExpenseTile.setText(I18n.t("dashboard.expense"));
        lblCountTile.setText(I18n.t("tx.count"));
        searchField.setPromptText(I18n.t("tx.search"));
        btnNew.setText("+ " + I18n.t("tx.new"));
        btnAdvanced.setText("\u2699 " + I18n.t("tx.advanced"));
        lblDateFrom.setText(I18n.t("tx.filter.from"));
        lblDateTo.setText(I18n.t("tx.filter.to"));
        lblMinAmount.setText(I18n.t("tx.filter.min"));
        lblMaxAmount.setText(I18n.t("tx.filter.max"));
        lblSort.setText(I18n.t("tx.filter.sort"));
        btnClearFilters.setText(I18n.t("tx.filter.clear"));
        btnExport.setText(I18n.t("tx.export"));
        miCsv.setText("CSV");
        miExcel.setText("Excel");
        lblEmptyTitle.setText(I18n.t("tx.empty.title"));
        lblEmptyDesc.setText(I18n.t("tx.empty.desc"));
    }

    private void setupFilter() {
        typeFilter.setItems(FXCollections.observableArrayList(
            I18n.t("tx.filterAll"), I18n.t("tx.filterIncome"), I18n.t("tx.filterExpense")));
        typeFilter.getSelectionModel().selectFirst();

        Category all = new Category(null, I18n.t("tx.filterAll"), null);
        categoryFilter.getItems().add(all);
        categoryFilter.getItems().addAll(categoryService.getAll());
        categoryFilter.getSelectionModel().selectFirst();

        sortCombo.setItems(FXCollections.observableArrayList(
            I18n.t("tx.sort.dateDesc"),
            I18n.t("tx.sort.dateAsc"),
            I18n.t("tx.sort.amountDesc"),
            I18n.t("tx.sort.amountAsc")));
        sortCombo.getSelectionModel().selectFirst();
    }

    private void applyFilter() {
        String kw = searchField.getText();
        int idx = typeFilter.getSelectionModel().getSelectedIndex();
        TransactionType ty = idx == 1 ? TransactionType.INCOME : idx == 2 ? TransactionType.EXPENSE : null;
        Category cat = categoryFilter.getValue();
        Long catId = (cat != null && cat.getId() != null) ? cat.getId() : null;

        LocalDate from = dateFromPicker.getValue();
        LocalDate to = dateToPicker.getValue();
        BigDecimal minAmt = parseAmountOrNull(minAmountField.getText());
        BigDecimal maxAmt = parseAmountOrNull(maxAmountField.getText());

        List<Transaction> result = masterData.stream()
            .filter(t -> kw == null || kw.isBlank()
                || t.getTitle().toLowerCase().contains(kw.toLowerCase())
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(kw.toLowerCase())))
            .filter(t -> ty == null || t.getType() == ty)
            .filter(t -> catId == null || catId.equals(t.getCategoryId()))
            .filter(t -> from == null || !t.getDate().isBefore(from))
            .filter(t -> to == null || !t.getDate().isAfter(to))
            .filter(t -> minAmt == null || t.getAmount().compareTo(minAmt) >= 0)
            .filter(t -> maxAmt == null || t.getAmount().compareTo(maxAmt) <= 0)
            .collect(Collectors.toList());

        int sortIdx = sortCombo.getSelectionModel().getSelectedIndex();
        Comparator<Transaction> comp = switch (sortIdx) {
            case 1 -> Comparator.comparing(Transaction::getDate);
            case 2 -> Comparator.comparing(Transaction::getAmount).reversed();
            case 3 -> Comparator.comparing(Transaction::getAmount);
            default -> Comparator.comparing(Transaction::getDate).reversed()
                .thenComparing(Transaction::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        };
        result.sort(comp);

        // Summary
        BigDecimal totalIncome = result.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = result.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblIncomeValue.setText(Formatter.money(totalIncome));
        lblExpenseValue.setText(Formatter.money(totalExpense));
        lblCountValue.setText(String.valueOf(result.size()));

        renderList(result);
    }

    private void renderList(List<Transaction> list) {
        txListContainer.getChildren().clear();
        if (list.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            txScroll.setVisible(false);
            txScroll.setManaged(false);
            return;
        }
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        txScroll.setVisible(true);
        txScroll.setManaged(true);

        for (Transaction t : list) {
            txListContainer.getChildren().add(buildTxCard(t));
        }
    }

    private HBox buildTxCard(Transaction t) {
        HBox card = new HBox();
        card.getStyleClass().add("tx-card");

        // Icon
        String icon = t.getType() == TransactionType.INCOME ? "\uD83D\uDCB0" : "\uD83D\uDCB8";
        // Try to get category icon (if available via name mapping)
        if (t.getCategoryName() != null) {
            icon = categoryIcon(t.getCategoryName());
        }
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("tx-card-icon");

        // Content
        VBox content = new VBox();
        content.getStyleClass().add("tx-card-content");
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("tx-card-title");

        StringBuilder sub = new StringBuilder();
        if (t.getCategoryName() != null) sub.append(t.getCategoryName());
        if (sub.length() > 0) sub.append(" · ");
        sub.append(Formatter.date(t.getDate()));
        if (t.getDescription() != null && !t.getDescription().isBlank()) {
            sub.append(" · ").append(t.getDescription());
        }
        Label subLbl = new Label(sub.toString());
        subLbl.getStyleClass().add("tx-card-sub");

        content.getChildren().addAll(title, subLbl);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Amount
        String sign = t.getType() == TransactionType.INCOME ? "+" : "\u2212";
        Label amount = new Label(sign + Formatter.money(t.getAmount()));
        amount.getStyleClass().add("tx-card-amount");
        amount.getStyleClass().add(t.getType() == TransactionType.INCOME ? "income" : "expense");

        // Actions
        HBox actions = new HBox();
        actions.getStyleClass().add("tx-card-actions");
        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("action-btn-text");
        editBtn.setTooltip(new Tooltip(I18n.t("tx.edit")));
        editBtn.setOnAction(e -> openForm(t));

        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("action-btn-text", "danger");
        delBtn.setTooltip(new Tooltip(I18n.t("tx.delete")));
        delBtn.setOnAction(e -> deleteTx(t));

        actions.getChildren().addAll(editBtn, delBtn);

        card.getChildren().addAll(iconLbl, content, amount, actions);
        return card;
    }

    private String categoryIcon(String name) {
        if (name == null) return "\uD83D\uDCB8";
        String n = name.toLowerCase();
        if (n.contains("food") || n.contains("خوراک")) return "\uD83C\uDF54";
        if (n.contains("transport") || n.contains("حمل")) return "\uD83D\uDE97";
        if (n.contains("housing") || n.contains("مسکن")) return "\uD83C\uDFE0";
        if (n.contains("salary") || n.contains("حقوق")) return "\uD83D\uDCB0";
        if (n.contains("shop") || n.contains("خرید")) return "\uD83D\uDECD";
        if (n.contains("health") || n.contains("سلامت")) return "\uD83C\uDFE5";
        if (n.contains("bill") || n.contains("قبض")) return "\uD83D\uDCC4";
        if (n.contains("entertain") || n.contains("سرگرمی")) return "\uD83C\uDFAE";
        return "\uD83D\uDCB8";
    }

    private BigDecimal parseAmountOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return new BigDecimal(s.trim().replace(",", "")); }
        catch (NumberFormatException e) { return null; }
    }

    @FXML private void clearFilters() {
        searchField.clear();
        typeFilter.getSelectionModel().selectFirst();
        categoryFilter.getSelectionModel().selectFirst();
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        minAmountField.clear();
        maxAmountField.clear();
        sortCombo.getSelectionModel().selectFirst();
    }

    @FXML private void addTransaction() { openForm(null); }

    private void deleteTx(Transaction t) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            String.format(I18n.t("tx.confirmDelete"), t.getTitle()));
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(t.getId());
                refresh();
                Toast.success(getWindow(), I18n.t("tx.deleted"));
            }
        });
    }

    @FXML private void exportCsv() {
        File f = chooseFileWithName(I18n.t("export.csv.title"), "CSV (.csv)", "*.csv", ".csv");
        if (f != null) try {
            CsvExporter.export(f, masterData, I18n.getLocale().getLanguage());
            Toast.success(getWindow(), I18n.t("export.success") + ": " + f.getName());
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    @FXML private void exportExcel() {
        File f = chooseFileWithName(I18n.t("export.excel.title"), "Excel (.xlsx)", "*.xlsx", ".xlsx");
        if (f != null) try {
            ExcelExporter.export(f, masterData, I18n.getLocale().getLanguage());
            Toast.success(getWindow(), I18n.t("export.success") + ": " + f.getName());
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    @FXML private void exportPdf() {
        File f = chooseFileWithName(I18n.t("export.pdf.title"), "PDF (.pdf)", "*.pdf", ".pdf");
        if (f != null) try {
            PdfExporter.export(f, masterData, I18n.getLocale().getLanguage());
            Toast.success(getWindow(), I18n.t("export.success") + ": " + f.getName());
        } catch (Exception e) { Toast.error(getWindow(), e.getMessage()); }
    }

    private File chooseFileWithName(String title, String desc, String pattern, String ext) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialFileName("ledgerx-" + LocalDate.now() + ext);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, pattern));
        return fc.showSaveDialog(getWindow());
    }

    private void openForm(Transaction t) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/io/github/vexisvx/ledgerx/view/transaction-form.fxml"));
            loader.setResources(I18n.getBundle());
            Scene scene = new Scene(loader.load());
            ThemeManager.apply(scene);
            TransactionFormController ctrl = loader.getController();
            ctrl.setTransaction(t);
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle(t == null ? I18n.t("form.title.new") : I18n.t("form.title.edit"));
            st.setScene(scene);
            st.showAndWait();
            refresh();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refresh() {
        masterData.setAll(service.getAll());
        applyFilter();
    }

    private javafx.stage.Window getWindow() {
        return txListContainer.getScene() != null ? txListContainer.getScene().getWindow() : null;
    }
}
