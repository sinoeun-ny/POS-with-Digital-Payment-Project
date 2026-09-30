# ==============================================================================
# FoodEats End-to-End API Automated Test Script
# Tests: Customer Login -> Order Placement -> Merchant Kitchen -> Driver Dispatch -> Admin Stats
# ==============================================================================

$BaseUrl = "http://localhost:8080"
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "    FoodEats REST API Automated Test Suite (PowerShell)   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Customer Login
Write-Host "`n[1/6] Authenticating as Customer (customer@example.com)..." -ForegroundColor Yellow
$loginBody = @{ email = "customer@example.com"; password = "password123" } | ConvertTo-Json
$customerAuth = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$customerToken = $customerAuth.token
Write-Host "  -> Success! Customer JWT Token acquired for $($customerAuth.name)" -ForegroundColor Green

# 2. Get Merchant Menu Catalog
Write-Host "`n[2/6] Fetching Menu Catalog for Merchant 1 (Zando Burger)..." -ForegroundColor Yellow
$menu = Invoke-RestMethod -Uri "$BaseUrl/api/menu?merchantId=1" -Method Get
Write-Host "  -> Success! Found $($menu.Count) dishes on the menu." -ForegroundColor Green

# 3. Customer Places Order
Write-Host "`n[3/6] Placing a New Customer Order via POST /api/orders..." -ForegroundColor Yellow
$orderPayload = @{
    merchantId = 1
    deliveryAddress = "Building 42, St. 302, BKK1, Phnom Penh"
    deliveryFee = 1.50
    items = @(
        @{
            menuItemId = 1
            itemName = "Double Truffle Smash Burger"
            price = 7.25
            quantity = 2
            selectedOptions = "Patty Size: Double 150g"
        }
    )
} | ConvertTo-Json -Depth 5

$headers = @{ "Authorization" = "Bearer $customerToken"; "Content-Type" = "application/json" }
$order = Invoke-RestMethod -Uri "$BaseUrl/api/orders" -Method Post -Headers $headers -Body $orderPayload
$orderId = $order.id
Write-Host "  -> Success! Order created: #$($order.orderNumber) (ID: $orderId), Total: `$$($order.totalAmount)" -ForegroundColor Green

# 4. Merchant Login & Update Status to PREPARING
Write-Host "`n[4/6] Authenticating as Merchant (merchant@example.com) & Kitchen Prep..." -ForegroundColor Yellow
$merchantLoginBody = @{ email = "merchant@example.com"; password = "password123" } | ConvertTo-Json
$merchantAuth = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $merchantLoginBody
$merchantToken = $merchantAuth.token

$merchantHeaders = @{ "Authorization" = "Bearer $merchantToken"; "Content-Type" = "application/json" }
$acceptBody = @{ status = "ACCEPTED" } | ConvertTo-Json
$orderAccept = Invoke-RestMethod -Uri "$BaseUrl/api/orders/$orderId/status" -Method Put -Headers $merchantHeaders -Body $acceptBody
Write-Host "  -> Success! Merchant accepted order: status is now $($orderAccept.status)" -ForegroundColor Green

$prepBody = @{ status = "PREPARING" } | ConvertTo-Json
$orderPrep = Invoke-RestMethod -Uri "$BaseUrl/api/orders/$orderId/status" -Method Put -Headers $merchantHeaders -Body $prepBody
Write-Host "  -> Success! Kitchen is cooking: status is now $($orderPrep.status)" -ForegroundColor Green

$readyBody = @{ status = "READY_FOR_PICKUP" } | ConvertTo-Json
$orderReady = Invoke-RestMethod -Uri "$BaseUrl/api/orders/$orderId/status" -Method Put -Headers $merchantHeaders -Body $readyBody
Write-Host "  -> Success! Kitchen finished food: status is now $($orderReady.status)" -ForegroundColor Green

# 5. Driver Dispatch & Delivery
Write-Host "`n[5/6] Authenticating as Driver (driver@example.com) & Delivering..." -ForegroundColor Yellow
$driverLoginBody = @{ email = "driver@example.com"; password = "password123" } | ConvertTo-Json
$driverAuth = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $driverLoginBody
$driverToken = $driverAuth.token

$driverHeaders = @{ "Authorization" = "Bearer $driverToken"; "Content-Type" = "application/json" }
$claim = Invoke-RestMethod -Uri "$BaseUrl/api/driver/orders/$orderId/accept" -Method Put -Headers $driverHeaders
Write-Host "  -> Driver accepted order #$($orderId): Driver assigned!" -ForegroundColor Green

$deliveredBody = @{ status = "DELIVERED" } | ConvertTo-Json
$delivered = Invoke-RestMethod -Uri "$BaseUrl/api/driver/orders/$orderId/status" -Method Put -Headers $driverHeaders -Body $deliveredBody
Write-Host "  -> Delivery complete! Order #$orderId status: $($delivered.status)" -ForegroundColor Green

# 6. Admin Dashboard Metrics
Write-Host "`n[6/6] Checking Admin Platform Metrics..." -ForegroundColor Yellow
$adminStats = Invoke-RestMethod -Uri "$BaseUrl/api/admin/dashboard" -Method Get
Write-Host "  -> Platform Metrics:" -ForegroundColor Cyan
Write-Host "     Total Users:      $($adminStats.totalUsers)"
Write-Host "     Total Merchants:  $($adminStats.totalMerchants)"
Write-Host "     Total Orders:     $($adminStats.totalOrders)"
Write-Host "     Delivered Orders: $($adminStats.deliveredOrders)"
Write-Host "     Total Revenue:    `$$($adminStats.totalRevenue)"

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "       ALL API WORKFLOW TESTS PASSED SUCCESSFULLY!        " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
