#!/usr/bin/env bash
set -euo pipefail
cd "$HOME/LedgerX"

echo ">>> [FIX SLF4J FINAL] جایگزینی کامل با logback"

# ============================================================
# 1) پاک کردن کامل تمام ارجاع به slf4j-simple از pom
# ============================================================
python3 - <<'PYEOF'
import re
path = "pom.xml"
with open(path) as f:
    src = f.read()

# حذف هر dependency با slf4j-simple یا slf4j-api توی pom
# (slf4j-api از transitive deps میاد، لازم نیست صریح باشه)

# حذف slf4j-simple
src = re.sub(
    r'<dependency>\s*<groupId>org\.slf4j</groupId>\s*<artifactId>slf4j-simple</artifactId>\s*<version>[^<]*</version>\s*</dependency>',
    '',
    src,
    flags=re.DOTALL
)

# حذف slf4j-api اگه صریح تعریف شده
src = re.sub(
    r'<dependency>\s*<groupId>org\.slf4j</groupId>\s*<artifactId>slf4j-api</artifactId>\s*<version>[^<]*</version>\s*</dependency>',
    '',
    src,
    flags=re.DOTALL
)

# اگه هنوز logback-classic نیست، اضافه کن
if 'logback-classic' not in src:
    # اضافه کردن قبل از </dependencies>
    src = src.replace(
        '</dependencies>',
        '''    <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.4.14</version>
        </dependency>
    </dependencies>''',
        1
    )

with open(path, "w") as f:
    f.write(src)
print(">>> pom.xml: slf4j-simple حذف، logback-classic اضافه")
PYEOF

# ============================================================
# 2) حذف slf4j.version از properties (اختیاری، لازم نیست)
# ============================================================

# ============================================================
# 3) پاک کردن کش کامل slf4j قدیمی از m2
# ============================================================
echo ">>> پاک کردن jar های slf4j قدیمی..."
find ~/.m2/repository/org/slf4j -type f -name "*.jar" -delete 2>/dev/null || true
rm -rf ~/.m2/repository/org/slf4j/slf4j-api/1.* 2>/dev/null || true
rm -rf ~/.m2/repository/org/slf4j/slf4j-simple 2>/dev/null || true

# ============================================================
# 4) پاک کردن کش target
# ============================================================
rm -rf target

# ============================================================
# 5) چک pom جدید
# ============================================================
echo ""
echo "=== dependencies مرتبط با لاگ ==="
grep -B1 -A3 "logback\|slf4j" pom.xml || echo "هیچ slf4j توی pom نیست، logback هست؟"
grep -B1 -A3 "logback" pom.xml

# ============================================================
# 6) rebuild کامل
# ============================================================
echo ""
echo ">>> rebuild..."
mvn -q clean compile 2>&1 | tail -20

# ============================================================
# 7) چک dependency tree
# ============================================================
echo ""
echo ">>> چک dependency tree برای slf4j:"
mvn dependency:tree 2>&1 | grep -i "slf4j\|logback" | head -10 || echo "(هیچ)"

echo ""
echo "=================================================="
echo ">>> [FIX SLF4J FINAL] DONE"
echo ""
echo ">>> حالا: mvn javafx:run"
echo "=================================================="
