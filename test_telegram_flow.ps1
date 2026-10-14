# ==============================================================================
# TELEGRAM BOT DRIVER DISPATCH & LIVE GPS TRACKING TEST SCRIPT
# Tests the full end-to-end lifecycle without needing an external Telegram server
# ==============================================================================

param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "`n🚀 Testing Telegram Bot Driver Dispatch & Live Tracking API at $BaseUrl`n" -ForegroundColor Cyan

# 1. Check Telegram Bot Status
Write-Host "1. Testing Telegram Bot Status..." -NoNewline
try {
    $status = Invoke-RestMethod -Uri "$BaseUrl/api/telegram/status" -Method GET
    Write-Host " OK" -ForegroundColor Green
    Write-Host "   Bot Configured: $($status.botConfigured) | Username: $($status.botUsername)" -ForegroundColor DarkGray
    Write-Host "   Connected Drivers: $($status.connectedDriversCount)" -ForegroundColor DarkGray
} catch {
    Write-Host " FAILED ($($_.Exception.Message))" -ForegroundColor Red
}

# 2. Register Driver Telegram Chat ID
Write-Host "`n2. Registering Driver Telegram Chat ID (Driver 3 -> Chat 987654321)..." -NoNewline
try {
    $regBody = @{
        driverId = 3
        chatId = 987654321
    } | ConvertTo-Json
    $regRes = Invoke-RestMethod -Uri "$BaseUrl/api/telegram/register-driver" -Method POST -Body $regBody -ContentType "application/json"
    Write-Host " OK" -ForegroundColor Green
    Write-Host "   Driver '$($regRes.driverName)' linked to Telegram Chat ID: $($regRes.telegramChatId)" -ForegroundColor DarkGray
} catch {
    Write-Host " FAILED ($($_.Exception.Message))" -ForegroundColor Red
}

# 3. Test Webhook: Driver Clicks '✅ Accept' on Order 1
Write-Host "`n3. Simulating Telegram Webhook: Driver Clicks '✅ Accept' (accept_1)..." -NoNewline
try {
    $acceptUpdate = @{
        update_id = 1001
        callback_query = @{
            id = "cbq_test_123"
            from = @{
                id = 987654321
                first_name = "Dara"
                username = "dara_driver"
            }
            message = @{
                message_id = 555
                chat = @{
                    id = 987654321
                    type = "private"
                }
                text = "🚨 NEW ORDER ALERT..."
            }
            data = "accept_1"
        }
    } | ConvertTo-Json -Depth 5

    $webhookRes = Invoke-RestMethod -Uri "$BaseUrl/api/telegram/webhook" -Method POST -Body $acceptUpdate -ContentType "application/json"
    Write-Host " OK (HTTP 200 Received)" -ForegroundColor Green
} catch {
    Write-Host " FAILED ($($_.Exception.Message))" -ForegroundColor Red
}

# 4. Simulating Telegram Live Location Stream (Driver moving)
Write-Host "`n4. Simulating Telegram Webhook: Driver Moves (Live GPS Location Stream)..." -NoNewline
try {
    $liveLocUpdate = @{
        update_id = 1002
        edited_message = @{
            message_id = 556
            chat = @{
                id = 987654321
                type = "private"
            }
            date = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
            location = @{
                latitude = 11.5564
                longitude = 104.9282
                heading = 85
                horizontal_accuracy = 4.2
                live_period = 3600
            }
        }
    } | ConvertTo-Json -Depth 5

    $locRes = Invoke-RestMethod -Uri "$BaseUrl/api/telegram/webhook" -Method POST -Body $liveLocUpdate -ContentType "application/json"
    Write-Host " OK (Live GPS Streamed)" -ForegroundColor Green
} catch {
    Write-Host " FAILED ($($_.Exception.Message))" -ForegroundColor Red
}

# 5. Verify Driver Live Location Endpoint
Write-Host "`n5. Verifying Driver Live GPS Location Endpoint (/api/driver/3/location)..." -NoNewline
try {
    $driverLoc = Invoke-RestMethod -Uri "$BaseUrl/api/driver/3/location" -Method GET
    Write-Host " OK" -ForegroundColor Green
    Write-Host "   Driver: $($driverLoc.driverName)" -ForegroundColor DarkGray
    Write-Host "   GPS: Lat $($driverLoc.latitude), Lng $($driverLoc.longitude) (Heading: $($driverLoc.heading)°)" -ForegroundColor DarkGray
    Write-Host "   Is Live Stream: $($driverLoc.isLive) | Updated: $($driverLoc.formattedTime)" -ForegroundColor DarkGray
} catch {
    Write-Host " FAILED ($($_.Exception.Message))" -ForegroundColor Red
}

# 6. Verify Customer Order Tracking Endpoint
Write-Host "`n6. Verifying Customer Order Tracking Endpoint (/api/orders/1/tracking)..." -NoNewline
try {
    $tracking = Invoke-RestMethod -Uri "$BaseUrl/api/orders/1/tracking" -Method GET
    Write-Host " OK" -ForegroundColor Green
    Write-Host "   Order: $($tracking.orderNumber) | Status: $($tracking.status)" -ForegroundColor DarkGray
    Write-Host "   Driver Assigned: $($tracking.driverName) ($($tracking.driverPhone))" -ForegroundColor DarkGray
    Write-Host "   Live Driver GPS: Lat $($tracking.driverLocation.latitude), Lng $($tracking.driverLocation.longitude)" -ForegroundColor DarkGray
    Write-Host "   Heading: $($tracking.driverLocation.heading)° | Accuracy: $($tracking.driverLocation.accuracy)m" -ForegroundColor DarkGray
} catch {
    Write-Host " FAILED ($($_.Exception.Message))" -ForegroundColor Red
}

Write-Host "`n🎉 All Telegram Bot Driver Dispatch & Live GPS Tracking Endpoints Tested!`n" -ForegroundColor Green
