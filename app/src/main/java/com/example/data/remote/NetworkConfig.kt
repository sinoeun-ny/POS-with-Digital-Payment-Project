package com.example.data.remote


/**
 * we define the available conneciton mode so the app can adapt to any network
 *
 */
enum class ConnectionMode(
    val title: String,
    val defaultUrl: String,
    val description: String
) {
    USB(
        title = "USB Cable",
        defaultUrl = "http://127.0.0.1:8080/",
        description = "Requires 'adb reverse tcp:8080 tcp:8080'. Zero Wi-Fi needed."
    ),

    EMULATOR(
        title = "Emulator",
        defaultUrl = "http://10.0.2.2:8080/",
        description = "Special loopback alias used by Android Emulator."
    ),

    WIFI(
        title = "Wi-Fi LAN",
        defaultUrl = "http://192.168.1.28:8080/",
        description = "Direct local computer IP on Wi-Fi."
    )
}