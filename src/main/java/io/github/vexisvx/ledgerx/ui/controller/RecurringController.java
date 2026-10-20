package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.RecurringTransaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.CategoryService;
import io.github.vexisvx.ledgerx.service.RecurringService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RecurringController {

    @FXML private Label lblTitle, lblSubtitle, lblEmptyTitle, lblEmptyDesc;
    @FXML private Label lblMonthlyLabel, lblActiveLabel, lblNextLabel;
    @FXML private Label lblMonthlyTotal, lblActiveCount, lblNextDate;
    @FXML private Button btnNew, btnProcess;
    @FXML private VBox recListContainer, emptyState;

    private final RecurringService service = new RecurringService();
    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        translate();
        render();
    }

    private void translate() {
        lblTitle.setText(I18n.t("rec.title"));
        btnNew.setText("+ " + I18n.t("rec.new"));
        btnProcess.setText(I18n.t("rec.process"));
        lblMonthlyLabel.setText(I18n.t("rec.monthlyTotal"));
        lblActiveLabel.setText(I18n.t("rec.activeCount"));
        lblNextLabel.setText(I18n.t("rec.nextPayment"));
        lblEmptyTitle.setText(I18n.t("rec.empty.title"));
        lblEmptyDesc.setText(I18n.t("rec.empty.desc"));
    }

    private void render() {
        recListContainer.getChildren().clear();
        List<RecurringTransaction> all = service.getAll();

        if (all.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            recListContainer.setVisible(false);
            recListContainer.setManaged(false);
            lblMonthlyTotal.setText(Formatter.money(BigDecimal.ZERO));
            lblActiveCount.setText("0");
            lblNextDate.setText("-");
            return;
        }
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        recListContainer.setVisible(true);
        recListContainer.setManaged(true);

        BigDecimal monthlyTotal = BigDecimal.ZERO;
        int activeCount = 0;
        LocalDate nextDue = null;

        for (RecurringTransaction r : all) {
            if (!r.isActive()) continue;
            activeCount++;
            if (r.getFrequency() == RecurringTransaction.Frequency.MONTHLY) {
                monthlyTotal = monthlyTotal.add(r.getAmount());
            } else if (r.getFrequency() == RecurringTransaction.Frequency.WEEKLY) {
                monthlyTotal = monthlyTotal.add(r.getAmount().multiply(BigDecimal.valueOf(4.33)));
            } else if (r.getFrequency() == RecurringTransaction.Frequency.YEARLY) {
                monthlyTotal = monthlyTotal.add(r.getAmount().divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP));
            } else if (r.getFrequency() == RecurringTransaction.Frequency.DAILY) {
                monthlyTotal = monthlyTotal.add(r.getAmount().multiply(BigDecimal.valueOf(30)));
            }
            if (nextDue == null || r.getNextDate().isBefore(nextDue)) {
                nextDue = r.getNextDate();
            }
        }

        lblMonthlyTotal.setText(Formatter.money(monthlyTotal));
        lblActiveCount.setText(String.valueOf(activeCount));
        lblNextDate.setText(nextDue != null ? Formatter.date(nextDue) : "-");

        for (RecurringTransaction r : all) {
            recListContainer.getChildren().add(buildRecItem(r));
        }
    }

    private HBox buildRecItem(RecurringTransaction r) {
        HBox item = new HBox();
        item.getStyleClass().add("rec-item");
        if (!r.isActive()) item.getStyleClass().add("inactive");

        String icon = r.getType() == TransactionType.INCOME ? "[+]" : "[-]";
        Label ic = new Label(icon);
        ic.getStyleClass().add("rec-icon");
        ic.setStyle("-fx-text-fill: " + (r.getType() == TransactionType.INCOME ? "#00d4aa" : "#ff6b6b") + "; -fx-font-weight: 900; -fx-font-size: 14px;");

        VBox body = new VBox();
        body.getStyleClass().add("rec-body");

        Label title = new Label(r.getTitle());
        title.getStyleClass().add("rec-title");

        String freq = freqLabel(r.getFrequency());
        String cat = r.getCategoryName() != null ? r.getCategoryName() : "-";
        String metaStr = cat + " \u00B7 " + freq + " \u00B7 " + I18n.t("rec.next") + " " + Formatter.date(r.getNextDate());
        Label meta = new Label(metaStr);
        meta.getStyleClass().add("rec-meta");

        body.getChildren().addAll(title, meta);
        HBox.setHgrow(body, Priority.ALWAYS);

        String sign = r.getType() == TransactionType.INCOME ? "+" : "\u2212";
        Label amount = new Label(sign + Formatter.money(r.getAmount()));
        amount.getStyleClass().add("rec-amount");
        amount.getStyleClass().add(r.getType() == TransactionType.INCOME ? "income" : "expense");

        Label badge = new Label();
        badge.getStyleClass().add("rec-badge");
        if (!r.isActive()) {
            badge.setText(I18n.t("rec.paused"));
            badge.getStyleClass().add("off");
        } else {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), r.getNextDate());
            if (days < 0) {
                badge.setText(I18n.t("rec.overdue"));
                badge.getStyleClass().add("overdue");
            } else if (days <= 3) {
                badge.setText(I18n.t("rec.soon"));
                badge.getStyleClass().add("soon");
            } else {
                badge.setText(I18n.t("rec.active"));
            }
        }

        HBox actions = new HBox();
        actions.getStyleClass().add("rec-actions");

        // Pause/Resume
        Button pauseBtn = new Button(r.isActive() ? "Pause" : "Resume");
        pauseBtn.getStyleClass().add("action-btn-text");
        pauseBtn.setTooltip(new Tooltip(r.isActive() ? I18n.t("rec.pause") : I18n.t("rec.resume")));
        pauseBtn.setOnAction(e -> toggleActive(r));

        // Edit
        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("action-btn-text");
        editBtn.setTooltip(new Tooltip(I18n.t("rec.edit")));
        editBtn.setOnAction(e -> editRec(r));

        // Delete
        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("action-btn-text", "danger");
        delBtn.setTooltip(new Tooltip(I18n.t("rec.delete")));
        delBtn.setOnAction(e -> deleteRec(r));

        actions.getChildren().addAll(pauseBtn, editBtn, delBtn);

        item.getChildren().addAll(ic, body, amount, badge, actions);
        return item;
    }

    private String freqLabel(RecurringTransaction.Frequency f) {
        return switch (f) {
            case DAILY -> I18n.t("rec.freq.daily");
            case WEEKLY -> I18n.t("rec.freq.weekly");
            case MONTHLY -> I18n.t("rec.freq.monthly");
            case YEARLY -> I18n.t("rec.freq.yearly");
        };
    }

    /** FIX: no validation on pause/resume — just toggle flag and save */
    private void toggleActive(RecurringTransaction r) {
        try {
            r.setActive(!r.isActive());
            service.update(r);
            render();
            Toast.success(getWindow(), r.isActive() ? I18n.t("rec.resumed") : I18n.t("rec.paused_msg"));
        } catch (Exception e) {
            // rollback
            r.setActive(!r.isActive());
            Toast.error(getWindow(), e.getMessage());
        }
    }

    @FXML
    private void add() {
        showRecurringDialog(null);
    }

    private void editRec(RecurringTransaction existing) {
        showRecurringDialog(existing);
    }

    private void showRecurringDialog(RecurringTransaction existing) {
        Dialog<RecurringTransaction> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? I18n.t("rec.new") : I18n.t("rec.edit"));
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType(I18n.t("form.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: -fx-bg;");
        root.setPrefWidth(400);

        Label titleLbl = new Label(I18n.t("form.title"));
        titleLbl.getStyleClass().add("form-label");
        TextField titleField = new TextField(existing != null ? existing.getTitle() : "");
        InputFilter.maxLength(titleField, 100);

        Label amountLbl = new Label(I18n.t("form.amount"));
        amountLbl.getStyleClass().add("form-label");
        TextField amountField = new TextField(existing != null
            ? existing.getAmount().toPlainString() : "");
        InputFilter.amountField(amountField);

        Label catLbl = new Label(I18n.t("form.category"));
        catLbl.getStyleClass().add("form-label");
        ComboBox<Category> catCombo = new ComboBox<>(
            FXCollections.observableArrayList(categoryService.getAll()));
        catCombo.setMaxWidth(Double.MAX_VALUE);
        if (existing != null) {
            for (Category c : catCombo.getItems()) {
                if (c.getId().equals(existing.getCategoryId())) {
                    catCombo.setValue(c);
                    break;
                }
            }
        }

        Label freqLbl = new Label(I18n.t("rec.frequency"));
        freqLbl.getStyleClass().add("form-label");
        ComboBox<RecurringTransaction.Frequency> freqCombo = new ComboBox<>(
            FXCollections.observableArrayList(RecurringTransaction.Frequency.values()));
        freqCombo.setValue(existing != null ? existing.getFrequency() : RecurringTransaction.Frequency.MONTHLY);
        freqCombo.setCellFactory(lv -> freqCell());
        freqCombo.setButtonCell(freqCell());
        freqCombo.setMaxWidth(Double.MAX_VALUE);

        Label dateLbl = new Label(I18n.t("rec.nextDate"));
        dateLbl.getStyleClass().add("form-label");
        DatePicker datePicker = new DatePicker(existing != null ? existing.getNextDate() : LocalDate.now().plusMonths(1));
        datePicker.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(titleLbl, titleField, amountLbl, amountField,
            catLbl, catCombo, freqLbl, freqCombo, dateLbl, datePicker);

        dialog.getDialogPane().setContent(root);
        if (dialog.getDialogPane().getScene() != null) {
            ThemeManager.apply(dialog.getDialogPane().getScene());
        }

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                try {
                    Validator.validateTitle(titleField.getText());
                    BigDecimal amount = Validator.parseAmount(amountField.getText());
                    Validator.validateAmount(amount);
                    Category cat = catCombo.getValue();
                    if (cat == null) throw new IllegalArgumentException("Select a category");
                    if (datePicker.getValue() == null) throw new IllegalArgumentException("Date is required");

                    RecurringTransaction r = existing != null ? existing : new RecurringTransaction();
                    r.setTitle(titleField.getText().trim());
                    r.setAmount(amount);
                    r.setCategoryId(cat.getId());
                    r.setType(cat.getType());
                    r.setFrequency(freqCombo.getValue());
                    r.setNextDate(datePicker.getValue());
                    if (existing == null) r.setActive(true);
                    return r;
                } catch (IllegalArgumentException e) {
                    Toast.error(getWindow(), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> {
            try {
                if (existing == null) service.create(r);
                else service.update(r);
                render();
                Toast.success(getWindow(), I18n.t("rec.saved"));
            } catch (Exception e) {
                Toast.error(getWindow(), e.getMessage());
            }
        });
    }

    private ListCell<RecurringTransaction.Frequency> freqCell() {
        return new ListCell<>() {
            @Override protected void updateItem(RecurringTransaction.Frequency f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? "" : freqLabel(f));
            }
        };
    }

    @FXML
    private void processDue() {
        try {
            int n = service.processDue();
            Toast.success(getWindow(), String.format(I18n.t("rec.processed"), n));
            render();
        } catch (Exception e) {
            Toast.error(getWindow(), e.getMessage());
        }
    }

    private void deleteRec(RecurringTransaction r) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            String.format(I18n.t("rec.confirmDelete"), r.getTitle()));
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                service.delete(r.getId());
                render();
                Toast.success(getWindow(), I18n.t("rec.deleted"));
            }
        });
    }

    private javafx.stage.Window getWindow() {
        return recListContainer.getScene() != null ? recListContainer.getScene().getWindow() : null;
    }
}
