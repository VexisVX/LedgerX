package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.CategoryService;
import io.github.vexisvx.ledgerx.service.TransactionService;
import io.github.vexisvx.ledgerx.ui.anim.Animations;
import io.github.vexisvx.ledgerx.ui.component.InputFilter;
import io.github.vexisvx.ledgerx.ui.component.Toast;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.util.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionFormController {

    @FXML private Label lblFormTitle, lblType, lblAmount, lblDate, lblTxTitle, lblCategory, lblDescription;
    @FXML private ComboBox<TransactionType> typeCombo;
    @FXML private TextField amountField, titleField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextArea descArea;
    @FXML private Button btnSave, btnCancel;
    @FXML private Label lblDescCount;

    private final TransactionService service = new TransactionService();
    private final CategoryService categoryService = new CategoryService();
    private Transaction editing;

    @FXML
    public void initialize() {
        translate();
        typeCombo.getItems().setAll(TransactionType.values());
        typeCombo.getSelectionModel().select(TransactionType.EXPENSE);
        typeCombo.setCellFactory(lv -> typeCell());
        typeCombo.setButtonCell(typeCell());
        datePicker.setValue(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // ⭐ فیلترها با InputFilter
        InputFilter.amountField(amountField);
        InputFilter.maxLength(titleField, 100);
        InputFilter.maxLength(descArea, 500);

        // شمارنده کاراکتر توضیحات
        if (lblDescCount != null) {
            descArea.textProperty().addListener((o, a, b) -> {
                int len = b == null ? 0 : b.length();
                lblDescCount.setText(len + " / 500");
                lblDescCount.setStyle("-fx-text-fill: " + (len > 450 ? "#ff6b6b" : "#6a7a8a")
                    + "; -fx-font-size: 10px;");
            });
            lblDescCount.setText("0 / 500");
        }

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now().plusDays(1)));
            }
        });

        typeCombo.valueProperty().addListener((o, a, b) -> reloadCategories(b));
        reloadCategories(typeCombo.getValue());
    }

    private ListCell<TransactionType> typeCell() {
        return new ListCell<>() {
            @Override protected void updateItem(TransactionType t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) setText("");
                else setText(t == TransactionType.INCOME ? I18n.t("form.type.income") : I18n.t("form.type.expense"));
            }
        };
    }

    private void translate() {
        lblFormTitle.setText(editing == null ? I18n.t("form.title.new") : I18n.t("form.title.edit"));
        lblType.setText(I18n.t("form.type"));
        lblAmount.setText(I18n.t("form.amount"));
        lblDate.setText(I18n.t("form.date"));
        lblTxTitle.setText(I18n.t("form.title"));
        lblCategory.setText(I18n.t("form.category"));
        lblDescription.setText(I18n.t("form.description"));
        btnSave.setText(I18n.t("form.save"));
        btnCancel.setText(I18n.t("form.cancel"));
    }

    private void reloadCategories(TransactionType type) {
        if (type == null) return;
        categoryCombo.getItems().setAll(categoryService.getByType(type));
        if (!categoryCombo.getItems().isEmpty()) categoryCombo.getSelectionModel().selectFirst();
    }

    public void setTransaction(Transaction t) {
        if (t == null) return;
        this.editing = t;
        translate();
        typeCombo.setValue(t.getType());
        amountField.setText(t.getAmount().toPlainString());
        datePicker.setValue(t.getDate());
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription() != null ? t.getDescription() : "");
        for (Category c : categoryCombo.getItems()) {
            if (c.getId() != null && c.getId().equals(t.getCategoryId())) {
                categoryCombo.setValue(c);
                break;
            }
        }
    }

    @FXML
    private void save() {
        try {
            if (typeCombo.getValue() == null)
                throw new IllegalArgumentException("Select transaction type");

            BigDecimal amount = Validator.parseAmount(amountField.getText());

            String title = titleField.getText();
            Validator.validateTitle(title);

            LocalDate date = datePicker.getValue();
            Validator.validateDate(date);

            String desc = descArea.getText();
            Validator.validateDescription(desc);

            Category cat = categoryCombo.getValue();
            if (cat == null)
                throw new IllegalArgumentException("Select a category");

            Transaction t = editing != null ? editing : new Transaction();
            t.setType(typeCombo.getValue());
            t.setAmount(amount);
            t.setDate(date);
            t.setTitle(title.trim());
            t.setDescription(desc != null && !desc.isBlank() ? desc.trim() : null);
            t.setCategoryId(cat.getId());

            if (editing == null) service.create(t);
            else service.update(t);

            Toast.success(getWindow(), editing == null ? "Transaction saved" : "Transaction updated");
            close();
        } catch (IllegalArgumentException e) {
            Toast.error(getWindow(), e.getMessage());
            Animations.shake(amountField);
        } catch (Exception e) {
            Toast.error(getWindow(), "Error: " + e.getMessage());
        }
    }

    @FXML private void cancel() { close(); }

    private void close() {
        ((Stage) amountField.getScene().getWindow()).close();
    }

    private javafx.stage.Window getWindow() {
        return amountField.getScene() != null ? amountField.getScene().getWindow() : null;
    }
}
