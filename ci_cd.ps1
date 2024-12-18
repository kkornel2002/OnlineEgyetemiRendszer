# Lokalis projekt mappa
$ProjectPath = "C:\Users\kovac\IdeaProjects\OnlineEgyetemiRendszer"

# Maven build
Write-Host "Build inditasa..."
cd $ProjectPath
mvn clean package

# Teszteles
Write-Host "Tesztfuttatas inditasa..."
mvn test

Write-Host "CI/CD folyamat befejezodott."
