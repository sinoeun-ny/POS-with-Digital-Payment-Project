package com.example.security

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

data class JwtPayload(
    val sub: String,              // Email or User ID
    val userId: Long,
    val name: String,
    val roles: List<String>,      // e.g. ["ROLE_CUSTOMER", "ROLE_ADMIN"]
    val exp: Long,               // Expiration timestamp in epoch seconds
    val iat: Long,               // Issued-at timestamp
    val iss: String = "smart-food-delivery-api"
)

object JwtTokenUtil {

    private const val DEFAULT_VALIDITY_SECONDS = 3600L * 24L // 24 hours

    fun generateSimulatedToken(
        userId: Long,
        email: String,
        name: String,
        roles: List<UserRole>
    ): String {
        val iat = System.currentTimeMillis() / 1000
        val exp = iat + DEFAULT_VALIDITY_SECONDS

        val headerJson = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }

        val rolesArray = JSONArray()
        roles.forEach { rolesArray.put(it.code) }

        val payloadJson = JSONObject().apply {
            put("sub", email)
            put("userId", userId)
            put("name", name)
            put("roles", rolesArray)
            put("iat", iat)
            put("exp", exp)
            put("iss", "smart-food-delivery-api")
        }

        val encodedHeader = base64UrlEncode(headerJson.toString())
        val encodedPayload = base64UrlEncode(payloadJson.toString())
        val mockSignature = base64UrlEncode("sig_spring_boot_jwt_secret_$userId")

        return "$encodedHeader.$encodedPayload.$mockSignature"
    }

    fun parseTokenPayload(token: String): JwtPayload? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payloadJsonStr = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP), StandardCharsets.UTF_8)
            val json = JSONObject(payloadJsonStr)

            val rolesList = mutableListOf<String>()
            val rolesArray = json.optJSONArray("roles")
            if (rolesArray != null) {
                for (i in 0 until rolesArray.length()) {
                    rolesList.add(rolesArray.getString(i))
                }
            }

            JwtPayload(
                sub = json.optString("sub", ""),
                userId = json.optLong("userId", 0L),
                name = json.optString("name", ""),
                roles = rolesList,
                exp = json.optLong("exp", 0L),
                iat = json.optLong("iat", 0L),
                iss = json.optString("iss", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isTokenExpired(token: String): Boolean {
        val payload = parseTokenPayload(token) ?: return true
        val currentEpoch = System.currentTimeMillis() / 1000
        return payload.exp < currentEpoch
    }

    private fun base64UrlEncode(input: String): String {
        return Base64.encodeToString(input.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
