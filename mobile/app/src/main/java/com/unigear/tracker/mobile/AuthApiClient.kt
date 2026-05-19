package com.unigear.tracker.mobile

import org.json.JSONObject
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AuthApiResult(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val name: String? = null,
    val email: String? = null
)

data class RequestItem(
    val id: Long,
    val equipmentName: String,
    val category: String,
    val description: String,
    val quantity: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class RequestApiResult(
    val success: Boolean,
    val message: String,
    val requests: List<RequestItem> = emptyList()
)

data class EquipmentItem(
    val id: Long,
    val name: String,
    val category: String,
    val description: String,
    val availableQuantity: Int,
    val totalQuantity: Int,
    val location: String,
    val condition: String,
    val createdAt: String
)

data class EquipmentApiResult(
    val success: Boolean,
    val message: String,
    val equipment: List<EquipmentItem> = emptyList()
)

data class UserProfile(
    val id: Long,
    val name: String,
    val email: String,
    val role: String? = "USER",
    val profilePictureUrl: String? = null,
    val createdAt: String? = null
)

data class UserProfileResult(
    val success: Boolean,
    val message: String,
    val user: UserProfile? = null
)

object AuthApiClient {

    const val DEFAULT_BACKEND_BASE_URL = "http://10.0.2.2:8080"

    @Volatile
    var backendBaseUrl: String = DEFAULT_BACKEND_BASE_URL
        private set

    private val authBaseUrl: String
        get() = "$backendBaseUrl/api/auth"

    private val requestsBaseUrl: String
        get() = "$backendBaseUrl/api/requests"

    private val equipmentBaseUrl: String
        get() = "$backendBaseUrl/api/equipment"

    fun setBackendBaseUrl(url: String?) {
        val sanitized = url?.trim()?.removeSuffix("/")
        backendBaseUrl = if (sanitized.isNullOrBlank()) DEFAULT_BACKEND_BASE_URL else sanitized
    }

    fun canReachBackend(): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$backendBaseUrl/api/auth/mobile/google?redirect_uri=unigear://auth").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = connection.responseCode
            code in 200..399
        } catch (_: Exception) {
            false
        } finally {
            connection?.disconnect()
        }
    }

    fun login(email: String, password: String): AuthApiResult {
        val payload = JSONObject()
            .put("email", email)
            .put("password", password)
        return postJson("$authBaseUrl/login", payload)
    }

    fun register(name: String, email: String, password: String): AuthApiResult {
        val payload = JSONObject()
            .put("name", name)
            .put("email", email)
            .put("password", password)
        return postJson("$authBaseUrl/register", payload)
    }

    fun getRequests(token: String): RequestApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(requestsBaseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                parseRequests(body)
            } else {
                RequestApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            RequestApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun createRequest(
        token: String,
        equipmentName: String,
        category: String,
        description: String,
        quantity: Int
    ): RequestApiResult {
        val payload = JSONObject()
            .put("equipmentName", equipmentName)
            .put("category", category)
            .put("description", description)
            .put("quantity", quantity)

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(requestsBaseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
            }

            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray())
                output.flush()
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                RequestApiResult(true, "Request created")
            } else {
                RequestApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            RequestApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun deleteRequest(token: String, requestId: Long): RequestApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$requestsBaseUrl/$requestId").openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)
            if (statusCode in 200..299) {
                RequestApiResult(true, "Request deleted")
            } else {
                RequestApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            RequestApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun getEquipment(): EquipmentApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(equipmentBaseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                try {
                    val json = JSONObject(body)
                    val success = json.optBoolean("success", true)
                    val message = json.optString("message", "Success")
                    
                    val equipmentArray = json.optJSONArray("equipment") ?: json.optJSONArray("data")
                    val equipment = mutableListOf<EquipmentItem>()
                    
                    if (equipmentArray != null) {
                        for (i in 0 until equipmentArray.length()) {
                            val item = equipmentArray.getJSONObject(i)
                            equipment.add(EquipmentItem(
                                id = item.optLong("id"),
                                name = item.optString("name"),
                                category = item.optString("category"),
                                description = item.optString("description"),
                                availableQuantity = item.optInt("availableQuantity", 0),
                                totalQuantity = item.optInt("totalQuantity", 0),
                                location = item.optString("location"),
                                condition = item.optString("condition"),
                                createdAt = item.optString("createdAt")
                            ))
                        }
                    }
                    
                    EquipmentApiResult(success, message, equipment)
                } catch (e: Exception) {
                    EquipmentApiResult(false, "Failed to parse equipment data", emptyList())
                }
            } else {
                EquipmentApiResult(false, parseErrorMessage(body, statusCode), emptyList())
            }
        } catch (_: Exception) {
            EquipmentApiResult(false, "Unable to connect to backend. Check server and network.", emptyList())
        } finally {
            connection?.disconnect()
        }
    }

    fun getUserProfile(token: String): UserProfileResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$authBaseUrl/profile").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                try {
                    val json = JSONObject(body)
                    val success = json.optBoolean("success", true)
                    val message = json.optString("message", "Success")
                    
                    val userObj = json.optJSONObject("user")
                    val user = if (userObj != null) {
                        UserProfile(
                            id = userObj.optLong("id"),
                            name = userObj.optString("name"),
                            email = userObj.optString("email"),
                            role = userObj.optString("role", "USER"),
                            profilePictureUrl = userObj.optString("profilePictureUrl"),
                            createdAt = userObj.optString("createdAt")
                        )
                    } else null

                    UserProfileResult(success, message, user)
                } catch (e: Exception) {
                    UserProfileResult(false, "Failed to parse user profile", null)
                }
            } else {
                UserProfileResult(false, parseErrorMessage(body, statusCode), null)
            }
        } catch (_: Exception) {
            UserProfileResult(false, "Unable to connect to backend. Check server and network.", null)
        } finally {
            connection?.disconnect()
        }
    }

    fun updateUserProfile(token: String, name: String): AuthApiResult {
        val payload = JSONObject()
            .put("name", name)

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$authBaseUrl/profile").openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
            }

            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray())
                output.flush()
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                AuthApiResult(true, "Profile updated successfully")
            } else {
                AuthApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            AuthApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun logout(): AuthApiResult {
        // Clear local authentication data
        // Optionally call backend to invalidate token
        return AuthApiResult(true, "Logout successful")
    }

    private fun postJson(url: String, payload: JSONObject): AuthApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
            }

            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray())
                output.flush()
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                parseSuccess(body)
            } else {
                AuthApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            AuthApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun readResponseBody(connection: HttpURLConnection, statusCode: Int): String {
        val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }

    private fun parseSuccess(body: String): AuthApiResult {
        if (body.isBlank()) {
            return AuthApiResult(true, "Success")
        }

        return try {
            val json = JSONObject(body)
            val token = json.optString("accessToken").takeIf { it.isNotBlank() }
            val message = json.optString("message").ifBlank { "Success" }
            val name = json.optString("name").takeIf { it.isNotBlank() }
            val email = json.optString("email").takeIf { it.isNotBlank() }
            AuthApiResult(true, message, token, name, email)
        } catch (_: Exception) {
            AuthApiResult(true, "Success")
        }
    }

    private fun parseErrorMessage(body: String, statusCode: Int): String {
        if (body.isBlank()) {
            return "Request failed ($statusCode)."
        }

        return try {
            val json = JSONObject(body)

            val directError = json.optString("error")
            if (directError.isNotBlank()) return directError

            val directMessage = json.optString("message")
            if (directMessage.isNotBlank()) return directMessage

            if (json.keys().hasNext()) {
                val firstKey = json.keys().next()
                val fieldError = json.optString(firstKey)
                if (fieldError.isNotBlank()) return fieldError
            }

            "Request failed ($statusCode)."
        } catch (_: Exception) {
            body
        }
    }

    private fun parseRequests(body: String): RequestApiResult {
        if (body.isBlank()) {
            return RequestApiResult(true, "Success", emptyList())
        }

        return try {
            val array = JSONArray(body)
            val requests = mutableListOf<RequestItem>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                requests.add(
                    RequestItem(
                        id = item.optLong("id", 0L),
                        equipmentName = item.optString("equipmentName"),
                        category = item.optString("category"),
                        description = item.optString("description"),
                        quantity = item.optInt("quantity", 1),
                        status = item.optString("status"),
                        createdAt = item.optString("createdAt"),
                        updatedAt = item.optString("updatedAt")
                    )
                )
            }

            RequestApiResult(true, "Success", requests)
        } catch (_: Exception) {
            RequestApiResult(false, "Failed to parse request list")
        }
    }
}
