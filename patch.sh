cd ~/LedgerX

# 1) ThemeManager.java
sed -i 's|// base.css اول (همه متغیرهای پیش‌فرض)|// base.css first (defines default theme variables)|' \
    src/main/java/io/github/vexisvx/ledgerx/ui/theme/ThemeManager.java

sed -i 's|// theme روی base override می‌کنه|// theme overrides base variables|' \
    src/main/java/io/github/vexisvx/ledgerx/ui/theme/ThemeManager.java

# 2) FontLoader.java
sed -i 's|// تلاش برای لود Vazirmatn از resource (اگه کاربر اضافه کرد)|// Try to load Vazirmatn from resources (if user added it)|' \
    src/main/java/io/github/vexisvx/ledgerx/ui/theme/FontLoader.java

sed -i 's|// اگه Vazirmatn نبود، از فونت‌های سیستمی استفاده کن|// Fallback to system fonts if Vazirmatn is not available|' \
    src/main/java/io/github/vexisvx/ledgerx/ui/theme/FontLoader.java

# چک
echo "=== چک ==="
grep -n "//.*[آ-ی]" src/main/java/io/github/vexisvx/ledgerx/ui/theme/*.java || echo "✓ هیچ کامنت فارسی نمونده"

# کامپایل
mvn -q clean compile 2>&1 | tail -5
