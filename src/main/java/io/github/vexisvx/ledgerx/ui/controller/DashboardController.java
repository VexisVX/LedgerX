package io.github.vexisvx.ledgerx.ui.controller;

import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.model.Goal;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.service.BalanceTimelineService;
import io.github.vexisvx.ledgerx.service.BudgetService;
import io.github.vexisvx.ledgerx.service.DashboardService;
import io.github.vexisvx.ledgerx.ui.anim.Animations;
import io.github.vexisvx.ledgerx.ui.component.Icons;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import io.github.vexisvx.ledgerx.util.Formatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblGreeting, lblDate, lblMonthLabel;
    @FXML private Label lblIncomeLabel, lblExpenseLabel, lblNetLabel;
    @FXML private Label lblIncomeValue, lblExpenseValue, lblNetValue;
    @FXML private Label lblTrendTitle, lblCategoryTitle, lblRecentTitle, lblBudgetTitle, lblGoalsTitle, lblInsightsTitle;
    @FXML private Label lblViewAll, lblNoTx, lblNoBudget, lblNoGoals, lblNoInsights;
    @FXML private CheckBox autoRefreshCheck;
    @FXML private Button btnRefresh, btnPrevMonth, btnNextMonth;
    @FXML private ToggleButton btnBarMode, btnLineMode;
    @FXML private StackPane chartTrendContainer;
    @FXML private PieChart chartCategory;
    @FXML private VBox categoryLegend, recentTxBox, budgetBox, goalsBox, insightsBox;
    @FXML private VBox timelineBox;
    @FXML private javafx.scene.layout.HBox timelineSummary;
    @FXML private VBox forecastPanel, forecastBox;
    @FXML private Label lblForecastTitle, lblForecastEmpty;
    @FXML private Label lblTimelineTitle, lblTimelineEmpty, lblTimelineStart, lblTimelineNet;
    @FXML private ComboBox<String> timelineRangeCombo;

    private final DashboardService service = new DashboardService();
    private final BalanceTimelineService timelineService = new BalanceTimelineService();
    private final BudgetService budgetService = new BudgetService();
    private Timeline autoRefreshTimeline;
    private MainController mainController;

    // Selected month
    private YearMonth selectedMonth = YearMonth.now();

    // Chart mode
    private boolean barMode = true;
    private BarChart<String, Number> barChart;
    private LineChart<String, Number> lineChart;

    @FXML
    public void initialize() {
        translate();
        setupChartToggle();
        rebuildChart();

        // ⭐ همه چیز در Platform.runLater — بعد از FXML load
        Platform.runLater(() -> {
            // ComboBox
            if (timelineRangeCombo != null) {
                timelineRangeCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                    "Last 7 days", "Last 30 days", "This month"));
                timelineRangeCombo.setValue("Last 30 days");
                timelineRangeCombo.valueProperty().addListener((o, a, b) -> {
                    System.out.println(">>> Combo changed to: " + b);
                    loadTimeline();
                });
                System.out.println(">>> ComboBox setup: " + timelineRangeCombo.getItems().size() + " items");
            } else {
                System.err.println(">>> timelineRangeCombo is NULL!");
            }

            // autoRefresh
            if (autoRefreshCheck != null) {
                autoRefreshCheck.selectedProperty().addListener((o, oldV, newV) -> {
                    if (newV) startAutoRefresh();
                    else stopAutoRefresh();
                });
                autoRefreshCheck.setSelected(true);
            }

            // Load data
            loadData();
        });
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    private void startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> loadData()));
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
        loadData();
    }

    @FXML
    private void previousMonth() {
        selectedMonth = selectedMonth.minusMonths(1);
        loadData();
    }

    @FXML
    private void nextMonth() {
        selectedMonth = selectedMonth.plusMonths(1);
        loadData();
    }

    @FXML
    private void setBarMode() {
        barMode = true;
        if (btnBarMode != null) btnBarMode.setSelected(true);
        if (btnLineMode != null) btnLineMode.setSelected(false);
        rebuildChart();
    }

    @FXML
    private void setLineMode() {
        barMode = false;
        if (btnBarMode != null) btnBarMode.setSelected(false);
        if (btnLineMode != null) btnLineMode.setSelected(true);
        rebuildChart();
    }

    private void setupChartToggle() {
        if (btnBarMode != null) btnBarMode.setSelected(barMode);
        if (btnLineMode != null) btnLineMode.setSelected(!barMode);
    }

    private void rebuildChart() {
        chartTrendContainer.getChildren().clear();
        if (barMode) {
            barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
            barChart.setLegendVisible(true);
            barChart.setAnimated(false);
            barChart.getStyleClass().add("dash-chart");
            VBox.setVgrow(barChart, Priority.ALWAYS);
            chartTrendContainer.getChildren().add(barChart);
        } else {
            lineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
            lineChart.setLegendVisible(true);
            lineChart.setAnimated(false);
            lineChart.getStyleClass().add("dash-chart");
            VBox.setVgrow(lineChart, Priority.ALWAYS);
            chartTrendContainer.getChildren().add(lineChart);
        }
        // ⭐ Critical: reload data into the new chart
        loadTrendChart(selectedMonth);
    }

    @FXML
    private void goToTransactions() {
        if (mainController != null) mainController.showTransactions();
    }

    private void translate() {
        lblGreeting.setText(I18n.t("dash.welcome"));
        lblIncomeLabel.setText(I18n.t("dashboard.income"));
        lblExpenseLabel.setText(I18n.t("dashboard.expense"));
        lblNetLabel.setText(I18n.t("dashboard.net"));
        lblTrendTitle.setText(I18n.t("dash.trend"));
        if (lblForecastTitle != null) lblForecastTitle.setText(I18n.t("dash.forecast"));
        if (lblTimelineTitle != null) lblTimelineTitle.setText(I18n.t("dash.timeline"));
        if (btnBarMode != null) btnBarMode.setText(I18n.t("dash.barMode"));
        if (btnLineMode != null) btnLineMode.setText(I18n.t("dash.lineMode"));
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

    private void loadData() {
        // Header — show selected month + today's date
        String monthName = selectedMonth.getMonth().toString().substring(0, 3) + " " + selectedMonth.getYear();
        lblMonthLabel.setText(monthName);

        // Show "Today: ..." as sub-text
        boolean isCurrentMonth = selectedMonth.equals(YearMonth.now());
        if (isCurrentMonth) {
            lblDate.setText("Today: " + Formatter.date(LocalDate.now()));
        } else {
            lblDate.setText("Viewing: " + monthName);
        }

        int year = selectedMonth.getYear();
        int month = selectedMonth.getMonthValue();

        BigDecimal mi = service.getMonthIncome(year, month);
        BigDecimal me = service.getMonthExpense(year, month);
        BigDecimal mNet = mi.subtract(me);

        lblIncomeValue.setText(Formatter.money(mi));
        lblExpenseValue.setText(Formatter.money(me));
        lblNetValue.setText(Formatter.money(mNet));

        loadTrendChart(selectedMonth);
        loadCategoryPie(year, month);
        loadRecentTransactions();
        loadBudgets();
        loadGoals();
        loadInsights(year, month, mi, me);
        loadTimeline();
        loadForecast();
    }

    private void loadTrendChart(YearMonth target) {
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName(I18n.t("dashboard.income"));
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName(I18n.t("dashboard.expense"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM");
        for (int i = 5; i >= 0; i--) {
            YearMonth d = target.minusMonths(i);
            String label = d.atDay(1).format(fmt);
            incomeSeries.getData().add(new XYChart.Data<>(label,
                service.getMonthIncome(d.getYear(), d.getMonthValue()).doubleValue()));
            expenseSeries.getData().add(new XYChart.Data<>(label,
                service.getMonthExpense(d.getYear(), d.getMonthValue()).doubleValue()));
        }

        if (barMode && barChart != null) {
            barChart.getData().clear();
            barChart.getData().addAll(incomeSeries, expenseSeries);
        } else if (!barMode && lineChart != null) {
            lineChart.getData().clear();
            lineChart.getData().addAll(incomeSeries, expenseSeries);
        }
    }

    private void loadCategoryPie(int year, int month) {
        chartCategory.getData().clear();
        categoryLegend.getChildren().clear();

        List<Map.Entry<String, BigDecimal>> top = service.getTopSpendingCategories(year, month, 5);

        if (top.isEmpty()) {
            Label empty = new Label(I18n.t("dashboard.topEmpty"));
            empty.getStyleClass().add("dash-empty");
            categoryLegend.getChildren().add(empty);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (var e : top) total = total.add(e.getValue());

        for (var e : top) {
            String key = e.getKey();
            String transKey = "category." + key.toLowerCase().trim();
            String translated = I18n.tOrNull(transKey);
            String display = translated != null ? translated : key;
            String color = Icons.categoryColor(key);

            PieChart.Data data = new PieChart.Data(display, e.getValue().doubleValue());
            chartCategory.getData().add(data);

            HBox row = new HBox(6);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Region dot = new Region();
            dot.setPrefSize(10, 10);
            dot.setMinSize(10, 10);
            dot.setMaxSize(10, 10);
            dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");

            Label name = new Label(display);
            name.setStyle("-fx-text-fill: #d8e0e8; -fx-font-size: 11px;");

            Label pct = new Label("\u2014");
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                double p = e.getValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                pct.setText(String.format("%.0f%%", p));
            }
            pct.setStyle("-fx-text-fill: #6a7a8a; -fx-font-size: 10px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(dot, name, spacer, pct);
            categoryLegend.getChildren().add(row);
        }

        Platform.runLater(() -> {
            for (int i = 0; i < chartCategory.getData().size() && i < top.size(); i++) {
                PieChart.Data d = chartCategory.getData().get(i);
                String color = Icons.categoryColor(top.get(i).getKey());
                if (d.getNode() != null) d.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
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

        Region dot = new Region();
        dot.setPrefSize(8, 8);
        dot.setMinSize(8, 8);
        dot.setMaxSize(8, 8);
        dot.setStyle("-fx-background-color: " + Icons.categoryColor(t.getCategoryName()) + "; -fx-background-radius: 4;");

        VBox content = new VBox(1);
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("dash-tx-title");
        String cat = t.getCategoryName() != null ? t.getCategoryName() : "-";
        Label sub = new Label(cat + " \u00B7 " + Formatter.date(t.getDate()));
        sub.getStyleClass().add("dash-tx-sub");
        content.getChildren().addAll(title, sub);
        HBox.setHgrow(content, Priority.ALWAYS);

        String sign = t.getType() == TransactionType.INCOME ? "+" : "\u2212";
        Label amount = new Label(sign + Formatter.money(t.getAmount()));
        amount.getStyleClass().add("dash-tx-amount");
        amount.getStyleClass().add(t.getType() == TransactionType.INCOME ? "income" : "expense");

        row.getChildren().addAll(dot, content, amount);
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

    private void loadInsights(int year, int month, BigDecimal mi, BigDecimal me) {
        insightsBox.getChildren().clear();
        List<String> insights = generateInsights(year, month, mi, me);
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

            Label bullet = new Label("\u2022");
            bullet.setStyle("-fx-text-fill: #00d4aa; -fx-font-size: 16px; -fx-font-weight: 900;");

            Label text = new Label(insight);
            text.getStyleClass().add("dash-insight-text");
            text.setWrapText(true);

            row.getChildren().addAll(bullet, text);
            HBox.setHgrow(text, Priority.ALWAYS);
            insightsBox.getChildren().add(row);
        }
    }

    private List<String> generateInsights(int year, int month, BigDecimal mi, BigDecimal me) {
        List<String> insights = new ArrayList<>();

        YearMonth prev = YearMonth.of(year, month).minusMonths(1);
        BigDecimal prevExp = service.getMonthExpense(prev.getYear(), prev.getMonthValue());
        if (prevExp.compareTo(BigDecimal.ZERO) > 0 && me.compareTo(BigDecimal.ZERO) > 0) {
            double pct = me.subtract(prevExp).doubleValue() / prevExp.doubleValue() * 100;
            if (Math.abs(pct) >= 5) {
                if (pct > 0) insights.add(String.format(I18n.t("dash.insight.spentMore"), Math.abs(pct)));
                else insights.add(String.format(I18n.t("dash.insight.spentLess"), Math.abs(pct)));
            }
        }

        if (mi.compareTo(BigDecimal.ZERO) > 0 && me.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal saved = mi.subtract(me);
            if (saved.compareTo(BigDecimal.ZERO) > 0)
                insights.add(String.format(I18n.t("dash.insight.saving"), Formatter.money(saved)));
            else
                insights.add(String.format(I18n.t("dash.insight.overspending"), Formatter.money(saved.abs())));
        }

        return insights;
    }

    private void loadTimeline() {
        if (timelineBox == null) return;

        timelineBox.getChildren().clear();

        String range = timelineRangeCombo != null ? timelineRangeCombo.getValue() : "Last 7 days";
        int days = "Last 7 days".equals(range) ? 7 : "Last 30 days".equals(range) ? 30 : 30;

        BalanceTimelineService.Timeline tl;
        if (I18n.t("timeline.thisMonth").equals(range)) {
            YearMonth now = YearMonth.now();
            tl = timelineService.buildTimeline(now.atDay(1), now.atEndOfMonth());
        } else {
            tl = timelineService.buildTimeline(days);
        }

        if (tl.entries.isEmpty()) {
            if (lblTimelineEmpty != null) {
                lblTimelineEmpty.setVisible(true);
                lblTimelineEmpty.setManaged(true);
            }
            if (timelineSummary != null) {
                timelineSummary.setVisible(false);
                timelineSummary.setManaged(false);
            }
            return;
        }

        if (lblTimelineEmpty != null) {
            lblTimelineEmpty.setVisible(false);
            lblTimelineEmpty.setManaged(false);
        }

        // Limit to last 20 entries
        List<BalanceTimelineService.Entry> shown = tl.entries;
        if (shown.size() > 20) shown = shown.subList(shown.size() - 20, shown.size());

        for (BalanceTimelineService.Entry e : shown) {
            HBox row = new HBox();
            row.getStyleClass().add("timeline-row");

            Region dot = new Region();
            dot.getStyleClass().add("timeline-dot");
            String dotColor = e.transaction.getType() == TransactionType.INCOME ? "#10b981" : "#ef4444";
            dot.setStyle("-fx-background-color: " + dotColor + ";");

            Label date = new Label(Formatter.date(e.date));
            date.getStyleClass().add("timeline-date");

            Label title = new Label(e.transaction.getTitle());
            title.getStyleClass().add("timeline-title");
            HBox.setHgrow(title, Priority.ALWAYS);

            String sign = e.transaction.getType() == TransactionType.INCOME ? "+" : "\u2212";
            Label amount = new Label(sign + Formatter.money(e.transaction.getAmount()));
            amount.getStyleClass().add("timeline-amount");
            amount.getStyleClass().add(e.transaction.getType() == TransactionType.INCOME ? "income" : "expense");

            Label balance = new Label(Formatter.money(e.balance));
            balance.getStyleClass().add("timeline-balance");

            row.getChildren().addAll(dot, date, title, amount, balance);
            timelineBox.getChildren().add(row);
        }

        // Summary
        if (timelineSummary != null && lblTimelineStart != null && lblTimelineNet != null) {
            timelineSummary.setVisible(true);
            timelineSummary.setManaged(true);
            lblTimelineStart.setText(String.format(I18n.t("timeline.start"), Formatter.money(tl.startBalance)));

            BigDecimal net = tl.netChange();
            String sign = net.signum() >= 0 ? "+" : "\u2212";
            lblTimelineNet.setText(String.format(I18n.t("timeline.net"), sign + Formatter.money(net.abs())));
            lblTimelineNet.getStyleClass().removeAll("positive", "negative");
            lblTimelineNet.getStyleClass().add(net.signum() >= 0 ? "positive" : "negative");
        }
    }

    private void loadForecast() {
        if (forecastBox == null) return;
        forecastBox.getChildren().clear();

        List<BudgetService.Forecast> alerts;
        try {
            alerts = budgetService.getAlerts();
        } catch (Exception e) {
            System.err.println(">>> Forecast error: " + e.getMessage());
            return;
        }

        System.out.println(">>> Forecast alerts: " + alerts.size());

        if (alerts.isEmpty()) {
            if (lblForecastEmpty != null) {
                lblForecastEmpty.setVisible(true);
                lblForecastEmpty.setManaged(true);
                lblForecastEmpty.setText(I18n.t("dash.forecastEmpty"));
            }
            return;
        }

        if (lblForecastEmpty != null) {
            lblForecastEmpty.setVisible(false);
            lblForecastEmpty.setManaged(false);
        }

        for (BudgetService.Forecast f : alerts) {
            VBox item = new VBox(8);
            item.getStyleClass().add("alert-item");
            if (f.willOverspend) item.getStyleClass().add("danger");

            HBox header = new HBox(8);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label icon = new Label(f.willOverspend ? "!" : "!");
            icon.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: " +
                (f.willOverspend ? "#ff6b6b" : "#f5a623") + "; -fx-min-width: 24; -fx-alignment: CENTER;");

            Label title = new Label(f.budget.getCategoryName());
            title.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: -fx-text-strong;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String status = f.willOverspend ? I18n.t("forecast.overBudget") : I18n.t("forecast.atRisk");
            Label statusLbl = new Label(status);
            statusLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-padding: 2 8; " +
                "-fx-background-radius: 8; -fx-background-color: " +
                (f.willOverspend ? "rgba(255,107,107,0.2)" : "rgba(245,166,35,0.2)") + "; " +
                "-fx-text-fill: " + (f.willOverspend ? "#ff6b6b" : "#f5a623") + ";");

            header.getChildren().addAll(icon, title, spacer, statusLbl);

            // Detail
            Label detail = new Label(
                String.format(I18n.t("forecast.spent"),
                    Formatter.money(f.budget.getSpent()),
                    Formatter.money(f.budget.getMonthlyLimit()),
                    (int) f.budget.getUsagePercent())
                + "  ·  " + String.format(I18n.t("forecast.daysLeft"), f.daysLeft));
            detail.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-text-sub;");

            // Progress
            ProgressBar bar = new ProgressBar(Math.min(f.budget.getUsagePercent() / 100.0, 1.0));
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setPrefHeight(8);
            bar.setStyle(f.willOverspend
                ? "-fx-accent: #ff6b6b;"
                : "-fx-accent: #f5a623;");

            // Projection + Suggestion
            Label projection = new Label();
            if (f.willOverspend) {
                BigDecimal over = f.overshoot.abs();
                projection.setText(String.format(I18n.t("forecast.projectedOver"),
                    Formatter.money(f.projectedSpend), Formatter.money(over)));
                projection.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff6b6b; -fx-font-weight: 700;");
            } else {
                projection.setText(String.format(I18n.t("forecast.projectedOk"),
                    Formatter.money(f.projectedSpend),
                    Formatter.money(f.budget.getMonthlyLimit())));
                projection.setStyle("-fx-font-size: 11px; -fx-text-fill: #f5a623; -fx-font-weight: 600;");
            }

            // Suggestion
            Label suggestion = new Label();
            if (f.willOverspend && f.daysLeft > 0) {
                BigDecimal reduce = f.overshoot.abs().divide(BigDecimal.valueOf(f.daysLeft), 2, java.math.RoundingMode.HALF_UP);
                suggestion.setText(String.format(I18n.t("forecast.reduceBy"), Formatter.money(reduce)));
            } else if (f.willOverspend) {
                suggestion.setText(I18n.t("forecast.noDays"));
            } else {
                suggestion.setText(I18n.t("forecast.watch"));
            }
            suggestion.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-accent; -fx-font-style: italic;");

            item.getChildren().addAll(header, detail, bar, projection, suggestion);
            forecastBox.getChildren().add(item);
        }
    }
}
