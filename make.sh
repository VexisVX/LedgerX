cd ~/LedgerX

# 1. jar جدید
mvn clean package -DskipTests
ls -lh target/LedgerX-2.0.0.jar

# 2. commit
git add -A
git commit -m "Release v2.0.1: fixes and improvements" || true
git push

# 3. tag
git tag -a v2.0.1 -m "v2.0.1"
git push origin v2.0.1

# 4. برو GitHub → Releases → Draft new → v2.0.1 → Attach jar → Publish
