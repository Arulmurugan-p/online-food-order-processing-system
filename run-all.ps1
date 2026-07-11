Write-Host "Starting Online Food Order Processing System..." -ForegroundColor Cyan

Write-Host "Starting Order Service & ActiveMQ..." -ForegroundColor Yellow
Start-Process java -ArgumentList "-jar order-service/target/order-service-1.0.0-SNAPSHOT.jar"

Write-Host "Waiting for ActiveMQ to initialize on port 61616..." -ForegroundColor Yellow
while (-not (Test-NetConnection -Port 61616 -ComputerName localhost -WarningAction SilentlyContinue).TcpTestSucceeded) {
    Start-Sleep -Seconds 2
}
Write-Host "ActiveMQ is online!" -ForegroundColor Green

Write-Host "Starting Payment Service..." -ForegroundColor Yellow
Start-Process java -ArgumentList "-jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar"

Write-Host "Starting Kitchen Service..." -ForegroundColor Yellow
Start-Process java -ArgumentList "-jar kitchen-service/target/kitchen-service-1.0.0-SNAPSHOT.jar"

Write-Host "Starting Delivery Service..." -ForegroundColor Yellow
Start-Process java -ArgumentList "-jar delivery-service/target/delivery-service-1.0.0-SNAPSHOT.jar"

Write-Host "Starting React UI..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/c cd food-order-ui && npm run dev"

Write-Host "All services started successfully!" -ForegroundColor Green
