package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.model.Goal;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.DashboardService;
import io.github.vexisvx.ledgerx.ui.anim.Animations;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.ui.theme.ChartColorManager;
import io.github.vexisvx.ledgerx.util.Formatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblGreeting, lblDate;
    // Quick Stats: Daily Avg, Days Left, Projected
    @FXML private Label lblQuickTitle, lblDailyLabel, lblDailyValue, lblDailyCompare;
    @FXML private Label lblDaysLeftLabel, lblDaysLeftValue, lblDaysLeftSub;
    @FXML private Label lblProjectedLabel, lblProjectedValue, lblProjectedSub;
    @FXML private Label lblQuickEmpty;
    @FXML private Label lblIncomeLabel, lblExpenseLabel, lblNetLabel, lblRemainingLabel;
    @FXML private Label lblIncomeValue, lblExpenseValue, lblNetValue, lblRemainingValue;
    @FXML private Label lblIncomeSub, lblExpenseSub, lblNetSub, lblRemainingSub;
    @FXML private Label lblTrendTitle, lblCategoryTitle, lblRecentTitle, lblBudgetTitle, lblGoalsTitle, lblInsightsTitle;
    @FXML private Label lblViewAll, lblNoTx, lblNoBudget, lblNoGoals, lblNoInsights;
    @FXML private CheckBox autoRefreshCheck;
    @FXML private Button btnRefresh;
    @FXML private BarChart<String, Number> chartTrend;
    @FXML private PieChart chartCategory;
    @FXML private VBox categoryLegend, recentTxBox, budgetBox, goalsBox, insightsBox;

    private final DashboardService service = new DashboardService();
    private Timeline autoRefreshTimeline;
    private String currentLangAtLoad;

    @FXML
    public void initialize() {
        translate();
        applyCardGradients();
        currentLangAtLoad = I18n.getLocale().getLanguage();
        loadData();

        // Auto-refresh
        autoRefreshCheck.selectedProperty().addListener((o, oldV, newV) -> {
            if (newV) startAutoRefresh();
            else stopAutoRefresh();
        });
        // Default: on
        autoRefreshCheck.setSelected(true);
    }

    private void startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (currentLangAtLoad.equals(I18n.getLocale().getLanguage())) {
                ChartColorManager.advanceRotation();
                loadData();
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }

    @FXML
    private void refreshAll() {
        Animations.pulse(btnRefresh);
        ChartColorManager.advanceRotation();
        loadData();
        playRefreshPulse();
    }

    private void playRefreshPulse() {
        try {
            if (chartTrend == null || chartTrend.getScene() == null) return;
            javafx.scene.Node root = chartTrend.getScene().getRoot();
            if (root == null) return;
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), root);
            ft.setFromValue(0.7);
            ft.setToValue(1.0);
            ft.play();
        } catch (Exception ignored) {}
    }

    @FXML
    private void goToTransactions() {
        // Find MainController via scene, call showTransactions()
        try {
            Object root = chartTrend.getScene().getRoot();
            var mainCtrlField = root.getClass().getDeclaredMethod("showTransactions");
            mainCtrlField.setAccessible(true);
            // actually main.fxml root is BorderPane, controller is MainController
        } catch (Exception ignored) {}
        // Fallback: nothing (View All is just visual)
    }

    private void translate() {
        String lang = I18n.getLocale().getLanguage();

        lblGreeting.setText(I18n.t("dash.welcome"));
        lblDate.setText(Formatter.date(LocalDate.now()));

        lblIncomeLabel.setText(I18n.t("dashboard.income"));
        lblExpenseLabel.setText(I18n.t("dashboard.expense"));
        lblNetLabel.setText(I18n.t("dashboard.net"));
        lblRemainingLabel.setText(I18n.t("dash.remaining"));
        lblIncomeSub.setText(I18n.t("dash.thisMonth"));
        lblExpenseSub.setText(I18n.t("dash.thisMonth"));
        lblNetSub.setText(I18n.t("dash.thisMonth"));
        lblRemainingSub.setText(I18n.t("dash.toSpend"));

        lblTrendTitle.setText(I18n.t("dash.trend"));
        lblCategoryTitle.setText(I18n.t("dash.category"));
        lblRecentTitle.setText(I18n.t("dash.recent"));
        lblBudgetTitle.setText(I18n.t("dash.budget"));
        lblGoalsTitle.setText(I18n.t("dash.goals"));
        lblInsightsTitle.setText(I18n.t("dash.insights"));

        lblViewAll.setText(I18n.t("dash.viewAll") + " \u2192");
        lblNoTx.setText(I18n.t("dash.noTx"));
        lblNoBudget.setText(I18n.t("dash.noBudget"));
        lblNoGoals.setText(I18n.t("dash.noGoals"));
        lblNoInsights.setText(I18n.t("dash.noInsights"));

        autoRefreshCheck.setText(I18n.t("dash.auto"));
        btnRefresh.setText(I18n.t("dash.refresh"));
    }

    private void applyCardGradients() {
        try {
            javafx.application.Platform.runLater(() -> {
                try {
                    if (lblIncomeValue == null || lblIncomeValue.getScene() == null) return;
                    var root = lblIncomeValue.getScene().getRoot();
                    if (root == null) return;
                    var cards = root.lookupAll(".dash-card");
                    int i = 0;
                    String[] grads = {"income-grad", "expense-grad", "net-grad", "remain-grad"};
                    for (var node : cards) {
                        if (i < grads.length) {
                            node.getStyleClass().add(grads[i]);
                        }
                        i++;
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    private void loadData() {
        LocalDate now = LocalDate.now();

        // Summary values
        BigDecimal mi = service.getMonthIncome(now.getYear(), now.getMonthValue());
        BigDecimal me = service.getMonthExpense(now.getYear(), now.getMonthValue());
        BigDecimal mNet = mi.subtract(me);

        // Remaining = income - expense this month (same as net for now)
        BigDecimal remaining = mNet.compareTo(BigDecimal.ZERO) > 0 ? mNet : BigDecimal.ZERO;

        lblIncomeValue.setText(Formatter.money(mi));
        lblExpenseValue.setText(Formatter.money(me));
        lblNetValue.setText(Formatter.money(mNet));
        lblRemainingValue.setText(Formatter.money(remaining));

        // Charts
        loadQuickStats();
        loadTrendChart(now);
        loadCategoryPie(now);

        // Lists
        loadRecentTransactions();
        loadBudgets();
        loadGoals();
        loadInsights(now, mi, me);
    }

    private void installPieTooltips() {
        try {
            if (chartCategory == null) return;
            var data = chartCategory.getData();
            for (int i = 0; i < data.size(); i++) {
                final PieChart.Data item = data.get(i);
                final String name = item.getName();
                final double value = item.getPieValue();
                if (item.getNode() != null) {
                    javafx.scene.control.Tooltip tip = new javafx.scene.control.Tooltip(
                        name + ": " + Formatter.money(java.math.BigDecimal.valueOf(value))
                    );
                    tip.setShowDelay(javafx.util.Duration.millis(200));
                    javafx.scene.control.Tooltip.install(item.getNode(), tip);
                } else {
                    final String n = name;
                    final double v = value;
                    item.nodeProperty().addListener((o, oldV, newV) -> {
                        if (newV != null) {
                            javafx.scene.control.Tooltip tip = new javafx.scene.control.Tooltip(
                                n + ": " + Formatter.money(java.math.BigDecimal.valueOf(v))
                            );
                            tip.setShowDelay(javafx.util.Duration.millis(200));
                            javafx.scene.control.Tooltip.install(newV, tip);
                        }
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadQuickStats() {
        try {
            if (lblQuickTitle == null) return;
            lblQuickTitle.setText(I18n.t("qs.title"));

            // Daily Average
            java.math.BigDecimal dailyAvg = service.getDailyAverageExpense();
            lblDailyLabel.setText(I18n.t("qs.dailyAvg"));
            lblDailyValue.setText(Formatter.money(dailyAvg));

            // Compare with previous month
            java.math.BigDecimal prev = service.getPreviousMonthDailyAvg();
            if (prev != null && prev.compareTo(java.math.BigDecimal.ZERO) > 0
                && dailyAvg.compareTo(java.math.BigDecimal.ZERO) > 0) {
                double pct = dailyAvg.subtract(prev).doubleValue() / prev.doubleValue() * 100;
                if (Math.abs(pct) >= 1) {
                    String arrow = pct > 0 ? "\u2191" : "\u2193";
                    String sign = pct > 0 ? "+" : "";
                    lblDailyCompare.setText(arrow + " " + sign + String.format("%.1f%%", pct)
                        + " " + I18n.t("qs.vsLast"));
                    lblDailyCompare.setStyle(pct > 0
                        ? "-fx-text-fill: #ff6b6b; -fx-font-size: 10px;"
                        : "-fx-text-fill: #00d4aa; -fx-font-size: 10px;");
                } else {
                    lblDailyCompare.setText(I18n.t("qs.same"));
                    lblDailyCompare.setStyle("-fx-text-fill: -fx-text-muted; -fx-font-size: 10px;");
                }
            } else {
                lblDailyCompare.setText("");
            }

            // Days Left
            int total = service.getDaysInMonth();
            int elapsed = service.getElapsedDays();
            int left = service.getDaysLeftInMonth();
            lblDaysLeftLabel.setText(I18n.t("qs.daysLeft"));
            if (left == 0) {
                lblDaysLeftValue.setText(I18n.t("qs.monthEnded"));
                lblDaysLeftSub.setText("");
            } else {
                lblDaysLeftValue.setText(String.valueOf(left));
                lblDaysLeftSub.setText(I18n.t("qs.ofDays").replace("%d", String.valueOf(total)));
            }

            // Projected
            java.math.BigDecimal projected = service.getProjectedMonthExpense();
            lblProjectedLabel.setText(I18n.t("qs.projected"));
            lblProjectedValue.setText(Formatter.money(projected));

            // Sub: projected savings or warning
            java.time.LocalDate now = java.time.LocalDate.now();
            java.math.BigDecimal income = service.getMonthIncome(now.getYear(), now.getMonthValue());
            if (income.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal savings = income.subtract(projected);
                if (savings.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    lblProjectedSub.setText("\u2192 " + I18n.t("qs.save")
                        + " " + Formatter.money(savings));
                    lblProjectedSub.setStyle("-fx-text-fill: #00d4aa; -fx-font-size: 10px;");
                } else {
                    lblProjectedSub.setText("\u26A0 " + I18n.t("qs.over"));
                    lblProjectedSub.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 10px;");
                }
                lblProjectedValue.setStyle("-fx-text-fill: -fx-text-strong; -fx-font-size: 22px; -fx-font-weight: 900;");
            } else {
                lblProjectedSub.setText("");
            }

            // Empty state
            if (!service.hasAnyData() || projected.compareTo(java.math.BigDecimal.ZERO) == 0) {
                if (lblQuickEmpty != null) {
                    lblQuickEmpty.setText(I18n.t("qs.empty"));
                    lblQuickEmpty.setVisible(true);
                    lblQuickEmpty.setManaged(true);
                }
            } else {
                if (lblQuickEmpty != null) {
                    lblQuickEmpty.setVisible(false);
                    lblQuickEmpty.setManaged(false);
                }
            }
        } catch (Exception e) {
            // silent
        }
    }

    private void loadTrendChart(LocalDate now) {
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName(I18n.t("dashboard.income"));
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName(I18n.t("dashboard.expense"));

        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM");

        for (int i = 5; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            String label = d.format(monthFmt);
            incomeSeries.getData().add(new XYChart.Data<>(label,
                service.getMonthIncome(d.getYear(), d.getMonthValue()).doubleValue()));
            expenseSeries.getData().add(new XYChart.Data<>(label,
                service.getMonthExpense(d.getYear(), d.getMonthValue()).doubleValue()));
        }
        chartTrend.getData().clear();
        chartTrend.getData().addAll(incomeSeries, expenseSeries);
        // Apply bar colors based on theme
        Platform.runLater(() -> {
            try {
                String incColor = ChartColorManager.getIncomeColor();
                String expColor = ChartColorManager.getExpenseColor();
                if (!incomeSeries.getData().isEmpty()) {
                    for (var d : incomeSeries.getData()) {
                        if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: " + incColor + ";");
                    }
                }
                if (!expenseSeries.getData().isEmpty()) {
                    for (var d : expenseSeries.getData()) {
                        if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: " + expColor + ";");
                    }
                }
            } catch (Exception ignored) {}
        });
        chartTrend.setLegendVisible(true);
        chartTrend.setAnimated(true);
    }

    private String[] currentColors = ChartColorManager.getPieColors();

    private void applyPieColors() {
        if (chartCategory == null) return;
        var data = chartCategory.getData();
        if (data == null) return;
        // Refresh colors from ChartColorManager (supports rotation)
        currentColors = ChartColorManager.getPieColors();
        for (int i = 0; i < data.size(); i++) {
            final int idx = i;
            final String color = currentColors[idx % currentColors.length];
            final PieChart.Data item = data.get(idx);
            final javafx.scene.Node node = item.getNode();
            if (node != null) {
                node.setStyle("-fx-pie-color: " + color + ";");
            } else {
                item.nodeProperty().addListener((o, oldV, newV) -> {
                    if (newV != null) newV.setStyle("-fx-pie-color: " + color + ";");
                });
            }
        }
    }

    private void loadCategoryPie(LocalDate now) {
        chartCategory.getData().clear();
        categoryLegend.getChildren().clear();

        List<Map.Entry<String, BigDecimal>> top = service.getTopSpendingCategories(
            now.getYear(), now.getMonthValue(), 5);

        if (top.isEmpty()) {
            chartCategory.getData().add(new PieChart.Data(I18n.t("dashboard.none"), 1));
            Label empty = new Label(I18n.t("dashboard.topEmpty"));
            empty.getStyleClass().add("dash-empty");
            categoryLegend.getChildren().add(empty);
            return;
        }

        // Pie + Legend
        String[] colors = {"#00d4aa", "#5b9dff", "#f5a623", "#ff6b6b", "#a855f7"};
        int idx = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (var e : top) total = total.add(e.getValue());

        for (var e : top) {
            String key = e.getKey();
            // Translate category name via i18n
            String transKey = "category." + key.toLowerCase().trim();
            String translated = I18n.tOrNull(transKey);
            String display = translated != null ? translated : key;

            PieChart.Data data = new PieChart.Data(display, e.getValue().doubleValue());
            chartCategory.getData().add(data);

            // Legend row
            HBox row = new HBox(6);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Region dot = new Region();
            dot.setPrefSize(10, 10);
            dot.setMinSize(10, 10);
            dot.setMaxSize(10, 10);
            dot.setStyle("-fx-background-color: " + colors[idx % colors.length] + "; -fx-background-radius: 5;");

            Label name = new Label(display);
            name.setStyle("-fx-text-fill: #d8e0e8; -fx-font-size: 11px;");

            Label pct = new Label("—");
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                double p = e.getValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                pct.setText(String.format("%.0f%%", p));
            }
            pct.setStyle("-fx-text-fill: #6a7a8a; -fx-font-size: 10px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(dot, name, spacer, pct);
            categoryLegend.getChildren().add(row);
            idx++;
        }

        // Apply consistent pie chart colors
        Platform.runLater(() -> {
            applyPieColors();
            installPieTooltips();
        });
    }

    private void loadRecentTransactions() {
        recentTxBox.getChildren().clear();
        List<Transaction> all = service.getAllTransactions().stream()
            .sorted(Comparator.comparing(Transaction::getDate).reversed()
                .thenComparing(Transaction::getId, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .collect(Collectors.toList());

        if (all.isEmpty()) {
            lblNoTx.setVisible(true);
            lblNoTx.setManaged(true);
            return;
        }
        lblNoTx.setVisible(false);
        lblNoTx.setManaged(false);

        for (Transaction t : all) {
            recentTxBox.getChildren().add(buildTxRow(t));
        }
    }

    private HBox buildTxRow(Transaction t) {
        HBox row = new HBox();
        row.getStyleClass().add("dash-tx-row");

        String icon = t.getType() == TransactionType.INCOME ? "\uD83D\uDCB0" : "\uD83D\uDCB8";
        Label ic = new Label(icon);
        ic.getStyleClass().add("dash-tx-icon");

        VBox content = new VBox(1);
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("dash-tx-title");

        String cat = t.getCategoryName() != null ? t.getCategoryName() : "-";
        Label sub = new Label(cat + " · " + Formatter.date(t.getDate()));
        sub.getStyleClass().add("dash-tx-sub");
        content.getChildren().addAll(title, sub);
        HBox.setHgrow(content, Priority.ALWAYS);

        String sign = t.getType() == TransactionType.INCOME ? "+" : "\u2212";
        Label amount = new Label(sign + Formatter.money(t.getAmount()));
        amount.getStyleClass().add("dash-tx-amount");
        amount.getStyleClass().add(t.getType() == TransactionType.INCOME ? "income" : "expense");

        row.getChildren().addAll(ic, content, amount);
        return row;
    }

    private void loadBudgets() {
        budgetBox.getChildren().clear();
        List<Budget> budgets = service.getCurrentBudgets();
        if (budgets.isEmpty()) {
            lblNoBudget.setVisible(true);
            lblNoBudget.setManaged(true);
            return;
        }
        lblNoBudget.setVisible(false);
        lblNoBudget.setManaged(false);

        for (Budget b : budgets) {
            HBox row = new HBox();
            row.getStyleClass().add("dash-progress-row");

            Label name = new Label(b.getCategoryName());
            name.getStyleClass().add("dash-progress-label");

            ProgressBar bar = new ProgressBar(Math.min(b.getUsagePercent() / 100.0, 1.0));
            bar.getStyleClass().add("dash-progress-bar");
            HBox.setHgrow(bar, Priority.ALWAYS);
            bar.setMaxWidth(Double.MAX_VALUE);
            if (b.getUsagePercent() >= 100) bar.getStyleClass().add("danger");
            else if (b.getUsagePercent() >= 80) bar.getStyleClass().add("warn");

            Label pct = new Label(String.format("%.0f%%", b.getUsagePercent()));
            pct.getStyleClass().add("dash-progress-value");

            row.getChildren().addAll(name, bar, pct);
            budgetBox.getChildren().add(row);
        }
    }

    private void loadGoals() {
        goalsBox.getChildren().clear();
        List<Goal> goals = service.getGoals();
        if (goals.isEmpty()) {
            lblNoGoals.setVisible(true);
            lblNoGoals.setManaged(true);
            return;
        }
        lblNoGoals.setVisible(false);
        lblNoGoals.setManaged(false);

        for (Goal g : goals.stream().limit(4).collect(Collectors.toList())) {
            HBox row = new HBox();
            row.getStyleClass().add("dash-progress-row");

            Label name = new Label(g.getTitle());
            name.getStyleClass().add("dash-progress-label");

            ProgressBar bar = new ProgressBar(Math.min(g.getProgressPercent() / 100.0, 1.0));
            bar.getStyleClass().add("dash-progress-bar");
            HBox.setHgrow(bar, Priority.ALWAYS);
            bar.setMaxWidth(Double.MAX_VALUE);

            Label pct = new Label(String.format("%.0f%%", g.getProgressPercent()));
            pct.getStyleClass().add("dash-progress-value");

            row.getChildren().addAll(name, bar, pct);
            goalsBox.getChildren().add(row);
        }
    }

    private void loadInsights(LocalDate now, BigDecimal mi, BigDecimal me) {
        insightsBox.getChildren().clear();
        List<String> insights = generateInsights(now, mi, me);
        if (insights.isEmpty()) {
            lblNoInsights.setVisible(true);
            lblNoInsights.setManaged(true);
            return;
        }
        lblNoInsights.setVisible(false);
        lblNoInsights.setManaged(false);

        for (String insight : insights) {
            HBox row = new HBox();
            row.getStyleClass().add("dash-insight-row");

            Label icon = new Label(">");
            icon.getStyleClass().add("dash-insight-icon");

            Label text = new Label(insight);
            text.getStyleClass().add("dash-insight-text");
            text.setWrapText(true);

            row.getChildren().addAll(icon, text);
            HBox.setHgrow(text, Priority.ALWAYS);
            insightsBox.getChildren().add(row);
        }
    }

    private List<String> generateInsights(LocalDate now, BigDecimal mi, BigDecimal me) {
        List<String> insights = new ArrayList<>();

        // 1. Compare expense vs last month
        LocalDate prev = now.minusMonths(1);
        BigDecimal prevExp = service.getMonthExpense(prev.getYear(), prev.getMonthValue());
        if (prevExp.compareTo(BigDecimal.ZERO) > 0 && me.compareTo(BigDecimal.ZERO) > 0) {
            double pct = me.subtract(prevExp).doubleValue() / prevExp.doubleValue() * 100;
            if (Math.abs(pct) >= 5) {
                if (pct > 0) {
                    insights.add(String.format(I18n.t("dash.insight.spentMore"), Math.abs(pct)));
                } else {
                    insights.add(String.format(I18n.t("dash.insight.spentLess"), Math.abs(pct)));
                }
            }
        }

        // 2. Budget status
        List<Budget> budgets = service.getCurrentBudgets();
        for (Budget b : budgets) {
            if (b.getUsagePercent() >= 100) {
                insights.add(String.format(I18n.t("dash.insight.overBudget"), b.getCategoryName()));
                break;
            } else if (b.getUsagePercent() >= 80) {
                insights.add(String.format(I18n.t("dash.insight.nearBudget"),
                    b.getCategoryName(), (int) b.getUsagePercent()));
                break;
            }
        }

        // 3. Savings projection
        if (mi.compareTo(BigDecimal.ZERO) > 0 && me.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal saved = mi.subtract(me);
            if (saved.compareTo(BigDecimal.ZERO) > 0) {
                insights.add(String.format(I18n.t("dash.insight.saving"), Formatter.money(saved)));
            } else {
                insights.add(String.format(I18n.t("dash.insight.overspending"), Formatter.money(saved.abs())));
            }
        }

        // 4. Daily average
        BigDecimal dailyAvg = service.getDailyAverageExpense();
        if (dailyAvg.compareTo(BigDecimal.ZERO) > 0) {
            insights.add(String.format(I18n.t("dash.insight.dailyAvg"), Formatter.money(dailyAvg)));
        }

        // 5. Biggest expense
        Transaction big = service.getBiggestExpenseThisMonth();
        if (big != null) {
            insights.add(String.format(I18n.t("dash.insight.biggest"),
                big.getTitle(), Formatter.money(big.getAmount())));
        }

        return insights;
    }
}
