package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Goal;
import io.github.vexisvx.ledgerx.service.GoalService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoalsController {

    @FXML private Label lblTitle, lblSubtitle, lblEmptyTitle, lblEmptyDesc;
    @FXML private Button btnNew;
    @FXML private TilePane goalGrid;
    @FXML private VBox emptyState;

    private final GoalService service = new GoalService();

    @FXML
    public void initialize() {
        translate();
        render();
    }

    private void translate() {
        lblTitle.setText(I18n.t("goal.title"));
        btnNew.setText(I18n.t("goal.new"));
        lblEmptyTitle.setText(I18n.t("goal.empty.title"));
        lblEmptyDesc.setText(I18n.t("goal.empty.desc"));
    }

    private void render() {
        goalGrid.getChildren().clear();
        List<Goal> goals = service.getAll();

        if (goals.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            goalGrid.setVisible(false);
            goalGrid.setManaged(false);
            return;
        }
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        goalGrid.setVisible(true);
        goalGrid.setManaged(true);

        for (Goal g : goals) {
            goalGrid.getChildren().add(buildGoalCard(g));
        }
    }

    private VBox buildGoalCard(Goal g) {
        VBox card = new VBox();
        card.getStyleClass().add("goal-card");

        // Header: icon + title + deadline
        HBox header = new HBox();
        header.getStyleClass().add("goal-card-header");

        Label icon = new Label("[G]");
        icon.setStyle("-fx-text-fill: -fx-accent; -fx-font-weight: 900; -fx-font-size: 16px;");
        icon.getStyleClass().add("goal-card-icon");

        VBox headerText = new VBox(2);
        Label title = new Label(g.getTitle());
        title.getStyleClass().add("goal-card-title");

        StringBuilder deadlineStr = new StringBuilder();
        if (g.getDeadline() != null) {
            deadlineStr.append(Formatter.date(g.getDeadline()));
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline());
            if (daysLeft > 0) deadlineStr.append("  ·  ").append(daysLeft).append(" ").append(I18n.t("goal.daysLeft"));
            else if (daysLeft < 0) deadlineStr.append("  ·  ").append(I18n.t("goal.overdue"));
        }
        Label deadline = new Label(deadlineStr.toString());
        deadline.getStyleClass().add("goal-card-deadline");

        headerText.getChildren().addAll(title, deadline);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().addAll(icon, headerText);

        // Amounts
        HBox amounts = new HBox();
        amounts.getStyleClass().add("goal-amount-row");
        Label saved = new Label(Formatter.money(g.getSavedAmount()));
        saved.getStyleClass().add("goal-amount-saved");
        Label target = new Label("/ " + Formatter.money(g.getTargetAmount()));
        target.getStyleClass().add("goal-amount-target");
        amounts.getChildren().addAll(saved, target);

        // Progress
        ProgressBar bar = new ProgressBar(Math.min(g.getProgressPercent() / 100.0, 1.0));
        bar.getStyleClass().add("goal-progress");
        bar.setMaxWidth(Double.MAX_VALUE);

        // Percent + remaining
        HBox meta = new HBox();
        meta.getStyleClass().add("goal-percent-row");
        Label pct = new Label(String.format("%.0f%%", g.getProgressPercent()));
        pct.getStyleClass().add("goal-percent");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        BigDecimal remaining = g.getTargetAmount().subtract(g.getSavedAmount());
        Label remainingLbl = new Label(I18n.t("goal.remaining") + ": " + Formatter.money(remaining));
        remainingLbl.getStyleClass().add("goal-remaining");

        meta.getChildren().addAll(pct, spacer, remainingLbl);

        // Actions
        HBox actions = new HBox();
        actions.getStyleClass().add("goal-actions");

        Button addBtn = new Button("💰 " + I18n.t("goal.addSaving"));
        addBtn.getStyleClass().add("goal-btn");
        addBtn.setOnAction(e -> addSaving(g));

        Button editBtn = new Button(I18n.t("btn.edit"));
        editBtn.getStyleClass().add("cat-action-btn");
        editBtn.setOnAction(e -> showEditDialog(g));

        Button delBtn = new Button(I18n.t("btn.delete"));
        delBtn.getStyleClass().addAll("cat-action-btn", "danger");
        delBtn.setOnAction(e -> deleteGoal(g));

        actions.getChildren().addAll(addBtn, new Region(), editBtn, delBtn);
        HBox.setHgrow(actions.getChildren().get(1), Priority.ALWAYS);

        card.getChildren().addAll(header, amounts, bar, meta, actions);
        return card;
    }

    private String goalIcon(String title) {
        if (title == null) return "\uD83C\uDFC6";
        String t = title.toLowerCase();
        if (t.contains("laptop") || t.contains("computer")) return "\uD83D\uDCBB";
        if (t.contains("phone")) return "\uD83D\uDCF1";
        if (t.contains("car")) return "\uD83D\uDE97";
        if (t.contains("trip") || t.contains("travel")) return "\u2708";
        if (t.contains("house") || t.contains("home")) return "\uD83C\uDFE0";
        if (t.contains("wedding")) return "\uD83D\uDC8D";
        if (t.contains("education") || t.contains("school")) return "\uD83C\uDF93";
        return "\uD83C\uDFC6";
    }

    @FXML
    private void add() {
        showEditDialog(null);
    }

    private void showEditDialog(Goal existing) {
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? I18n.t("goal.new") : I18n.t("goal.edit"));
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType(I18n.t("form.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: -fx-bg;");
        root.setPrefWidth(380);

        Label nameLbl = new Label(I18n.t("goal.name"));
        nameLbl.getStyleClass().add("form-label");
        TextField nameField = new TextField(existing != null ? existing.getTitle() : "");
        InputFilter.maxLength(nameField, 50);

        Label targetLbl = new Label(I18n.t("goal.target"));
        targetLbl.getStyleClass().add("form-label");
        TextField targetField = new TextField(existing != null
            ? existing.getTargetAmount().toPlainString() : "");
        InputFilter.amountField(targetField);

        Label savedLbl = new Label(I18n.t("goal.saved"));
        savedLbl.getStyleClass().add("form-label");
        TextField savedField = new TextField(existing != null
            ? existing.getSavedAmount().toPlainString() : "0");
        InputFilter.amountField(savedField);

        Label dateLbl = new Label(I18n.t("goal.deadline"));
        dateLbl.getStyleClass().add("form-label");
        DatePicker datePicker = new DatePicker(existing != null
            ? existing.getDeadline() : LocalDate.now().plusMonths(6));
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        root.getChildren().addAll(nameLbl, nameField, targetLbl, targetField,
            savedLbl, savedField, dateLbl, datePicker);

        dialog.getDialogPane().setContent(root);
        if (dialog.getDialogPane().getScene() != null) {
            ThemeManager.apply(dialog.getDialogPane().getScene());
        }

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                try {
                    Validator.validateName(nameField.getText());
                    BigDecimal target = Validator.parseAmount(targetField.getText());
                    BigDecimal saved = BigDecimal.ZERO;
                    if (savedField.getText() != null && !savedField.getText().isBlank()) {
                        saved = Validator.parseAmount(savedField.getText());
                    }
                    LocalDate deadline = datePicker.getValue();
                    Validator.validateGoal(target, saved, deadline);

                    Goal g = existing != null ? existing : new Goal();
                    g.setTitle(nameField.getText().trim());
                    g.setTargetAmount(target);
                    g.setSavedAmount(saved);
                    g.setDeadline(deadline);
                    return g;
                } catch (IllegalArgumentException e) {
                    Toast.error(getWindow(), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            try {
                if (existing == null) {
                    service.create(g);
                } else {
                    service.update(g);
                }
                render();
                Toast.success(getWindow(), I18n.t("goal.saved"));
            } catch (Exception e) {
                Toast.error(getWindow(), e.getMessage());
            }
        });
    }

    private void addSaving(Goal g) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(I18n.t("goal.addSaving"));
        d.setContentText(I18n.t("goal.amountPrompt"));
        d.showAndWait().ifPresent(txt -> {
            try {
                BigDecimal amount = Validator.parseAmount(txt);
                service.addSaving(g, amount);
                render();
                Toast.success(getWindow(), I18n.t("goal.saved_ok"));
            } catch (Exception e) {
                Toast.error(getWindow(), e.getMessage());
            }
        });
    }

    private void deleteGoal(Goal g) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            String.format(I18n.t("goal.confirmDelete"), g.getTitle()));
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                service.delete(g.getId());
                render();
                Toast.success(getWindow(), I18n.t("goal.deleted"));
            }
        });
    }

    private javafx.stage.Window getWindow() {
        return goalGrid.getScene() != null ? goalGrid.getScene().getWindow() : null;
    }
}
