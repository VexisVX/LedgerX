cd ~/LedgerX

# اضافه کردن مستقیم با cat
cat >> src/main/resources/i18n/messages.properties <<'EOF'

# ─── Timeline (added) ───
timeline.start=Start: %s
timeline.net=Net: %s
timeline.last7=Last 7 days
timeline.last30=Last 30 days
timeline.thisMonth=This month

# ─── Forecast (added) ───
forecast.spent=Spent %s of %s (%d%%)
forecast.daysLeft=%d days left
forecast.overBudget=OVER BUDGET
forecast.atRisk=AT RISK
forecast.projectedOver=Projected: %s by end of month — %s over
forecast.projectedOk=Projected: %s of %s — on track
forecast.reduceBy=Reduce by %s/day to stay on track
forecast.noDays=No days left to adjust
forecast.watch=Watch your spending for the rest of the month

# ─── Chart Switch (added) ───
dash.barMode=Bar
dash.lineMode=Line
EOF

# چک
echo "=== EN keys now ==="
grep -E "^(timeline|forecast|dash\.(barMode|lineMode))" src/main/resources/i18n/messages.properties

# rebuild
mvn clean compile 2>&1 | tail -5

# چک target
echo ""
echo "=== target ==="
grep -c "timeline.start\|dash.barMode" target/classes/i18n/messages.properties
