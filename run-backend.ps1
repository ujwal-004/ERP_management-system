# Refresh environment variables first to register Java installation
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
$env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
if (!$env:JAVA_HOME) {
    $env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
}

Write-Host "=============================================" -ForegroundColor Green
Write-Host "    ERP MANAGEMENT SYSTEM: BACKEND START" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Verify java version
java -version
if ($LastExitCode -ne 0 -and !(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java JDK is not detected in your PATH. Please make sure Java JDK 17+ is installed and configured."
    Exit 1
}

# Download Maven if not present
$mavenVersion = "3.9.6"
$mavenDir = "$Home\.maven"
$mavenZip = "$mavenDir\apache-maven-$mavenVersion-bin.zip"
$mavenBin = "$mavenDir\apache-maven-$mavenVersion\bin"

if (!(Test-Path "$mavenBin\mvn.cmd")) {
    Write-Host "Maven not found locally. Downloading a portable version (Apache Maven $mavenVersion)..." -ForegroundColor Cyan
    if (!(Test-Path $mavenDir)) {
        New-Item -ItemType Directory -Force -Path $mavenDir | Out-Null
    }
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip" -OutFile $mavenZip
    Write-Host "Extracting Maven..." -ForegroundColor Cyan
    Expand-Archive -Path $mavenZip -DestinationPath $mavenDir -Force
    Remove-Item $mavenZip -Force
}

# Append Maven bin directory to current session Path
$env:Path = "$mavenBin;" + $env:Path

Write-Host "Maven ready: $(mvn -v | Select-Object -First 1)" -ForegroundColor Cyan
Write-Host "Running backend application..." -ForegroundColor Green
mvn spring-boot:run
