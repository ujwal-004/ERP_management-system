Write-Host "=============================================" -ForegroundColor Green
Write-Host "    ERP MANAGEMENT SYSTEM: FRONTEND START" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Check if Python is installed
$pythonCheck = python --version 2>$null
if ($LastExitCode -eq 0 -or $pythonCheck) {
    Write-Host "Starting Python HTTP Server on http://localhost:8081 ..." -ForegroundColor Cyan
    Write-Host "Opening web browser to the Login page..." -ForegroundColor Green
    Start-Process "http://localhost:8081/modules/users/login.html"
    cd frontend
    python -m http.server 8081
    Exit
}

# Check if Node is installed
$nodeCheck = node -v 2>$null
if ($LastExitCode -eq 0 -or $nodeCheck) {
    Write-Host "Starting Node.js http-server on http://localhost:8081 ..." -ForegroundColor Cyan
    Write-Host "Opening web browser to the Login page..." -ForegroundColor Green
    Start-Process "http://localhost:8081/modules/users/login.html"
    cd frontend
    npx http-server -p 8081
    Exit
}

Write-Error "Neither Node.js nor Python is available to host the static frontend files. Please open frontend/modules/users/login.html directly in your web browser."
