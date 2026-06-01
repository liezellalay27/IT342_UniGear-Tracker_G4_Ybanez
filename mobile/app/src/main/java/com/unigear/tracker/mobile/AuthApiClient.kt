package com.unigear.tracker.mobile

import org.json.JSONObject
import org.json.JSONArray
import java.io.BufferedWriter
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class AuthApiResult(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class RequestItem(
    val id: Long,
    val equipmentName: String,
    val category: String,
    val description: String,
    val quantity: Int,
    val requesterName: String,
    val requesterEmail: String,
    val borrowDate: String,
    val returnDate: String,
    val studentName: String,
    val schoolIdNumber: String,
    val yearLevel: String,
    val course: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class AdminUserItem(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String
)

data class AdminUserApiResult(
    val success: Boolean,
    val message: String,
    val users: List<AdminUserItem> = emptyList()
)

data class SimpleApiResult(
    val success: Boolean,
    val message: String
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

    private val profileBaseUrl: String
        get() = "$backendBaseUrl/api/profile"

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

    fun requestPasswordReset(email: String): AuthApiResult {
        val payload = JSONObject()
            .put("email", email)
        return postJson("$authBaseUrl/forgot-password", payload)
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

    fun getAdminRequests(token: String): RequestApiResult {
        return getJsonArrayList("$backendBaseUrl/api/admin/requests", token) { item ->
            parseRequestItem(item)
        }
    }

    fun getBorrowedRequests(token: String): RequestApiResult {
        return getJsonArrayList("$backendBaseUrl/api/admin/borrowed", token) { item ->
            parseRequestItem(item)
        }
    }

    fun getAdminUsers(token: String): AdminUserApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$backendBaseUrl/api/admin/users").openConnection() as HttpURLConnection).apply {
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
                    val array = JSONArray(body)
                    val users = mutableListOf<AdminUserItem>()
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        users.add(
                            AdminUserItem(
                                id = item.optLong("id", 0L),
                                name = item.optString("name"),
                                email = item.optString("email"),
                                role = item.optString("role"),
                                createdAt = item.optString("createdAt")
                            )
                        )
                    }
                    AdminUserApiResult(true, "Success", users)
                } catch (_: Exception) {
                    AdminUserApiResult(false, "Failed to parse admin users")
                }
            } else {
                AdminUserApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            AdminUserApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun updateAdminRequestStatus(
        token: String,
        requestId: Long,
        status: String,
        notes: String? = null,
        returnedOnTime: Boolean? = null
    ): RequestApiResult {
        val payload = JSONObject()
            .put("status", status)
            .put("notes", notes ?: JSONObject.NULL)
            .put("returnedOnTime", returnedOnTime ?: JSONObject.NULL)

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$backendBaseUrl/api/admin/requests/$requestId/status").openConnection() as HttpURLConnection).apply {
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
                RequestApiResult(true, "Request updated")
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
        quantity: Int,
        borrowDate: String,
        returnDate: String,
        studentName: String,
        schoolIdNumber: String,
        yearLevel: String,
        course: String
    ): RequestApiResult {
        var connection: HttpURLConnection? = null
        return try {
            val boundary = "----UniGearBoundary${UUID.randomUUID()}"
            connection = (URL(requestsBaseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
            }

            connection.outputStream.use { output ->
                val writer = BufferedWriter(OutputStreamWriter(output, Charsets.UTF_8))
                fun writeField(name: String, value: String) {
                    writer.append("--").append(boundary).append("\r\n")
                    writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n")
                    writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
                    writer.append(value).append("\r\n")
                }

                writeField("equipmentName", equipmentName)
                writeField("category", category)
                writeField("description", description)
                writeField("quantity", quantity.toString())
                writeField("borrowDate", borrowDate)
                writeField("returnDate", returnDate)
                writeField("studentName", studentName)
                writeField("schoolIdNumber", schoolIdNumber)
                writeField("yearLevel", yearLevel)
                writeField("course", course)

                writer.append("--").append(boundary).append("--\r\n")
                writer.flush()
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

    fun getEquipment(token: String? = null): EquipmentApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(equipmentBaseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                try {
                    val equipment = mutableListOf<EquipmentItem>()
                    val trimmed = body.trimStart()

                    when {
                        trimmed.startsWith("[") -> {
                            val array = JSONArray(body)
                            for (i in 0 until array.length()) {
                                equipment.add(parseEquipmentItem(array.getJSONObject(i)))
                            }
                            EquipmentApiResult(true, "Success", equipment)
                        }
                        else -> {
                            val json = JSONObject(body)
                            val success = json.optBoolean("success", true)
                            val message = json.optString("message", "Success")
                            val equipmentArray = json.optJSONArray("equipment") ?: json.optJSONArray("data")

                            if (equipmentArray != null) {
                                for (i in 0 until equipmentArray.length()) {
                                    equipment.add(parseEquipmentItem(equipmentArray.getJSONObject(i)))
                                }
                            }

                            EquipmentApiResult(success, message, equipment)
                        }
                    }
                } catch (_: Exception) {
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

    fun createEquipment(
        token: String,
        name: String,
        category: String,
        location: String,
        description: String,
        specifications: List<String>,
        totalQuantity: Int,
        availableQuantity: Int,
        status: String? = null
    ): SimpleApiResult {
        val payload = JSONObject()
            .put("name", name)
            .put("category", category)
            .put("location", location)
            .put("description", description)
            .put("specifications", JSONArray(specifications))
            .put("totalQuantity", totalQuantity)
            .put("availableQuantity", availableQuantity)

        if (!status.isNullOrBlank()) {
            payload.put("status", status)
        }

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(equipmentBaseUrl).openConnection() as HttpURLConnection).apply {
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
                SimpleApiResult(true, "Equipment created")
            } else {
                SimpleApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            SimpleApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun updateEquipment(token: String, id: Long, name: String, category: String, location: String, description: String, specifications: List<String>, totalQuantity: Int, availableQuantity: Int, status: String? = null): SimpleApiResult {
        val payload = JSONObject()
            .put("name", name)
            .put("category", category)
            .put("location", location)
            .put("description", description)
            .put("specifications", JSONArray(specifications))
            .put("totalQuantity", totalQuantity)
            .put("availableQuantity", availableQuantity)

        if (!status.isNullOrBlank()) {
            payload.put("status", status)
        }

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$equipmentBaseUrl/$id").openConnection() as HttpURLConnection).apply {
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
                SimpleApiResult(true, "Equipment updated")
            } else {
                SimpleApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            SimpleApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun deleteEquipment(token: String, id: Long): SimpleApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$equipmentBaseUrl/$id").openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                SimpleApiResult(true, "Equipment deleted")
            } else {
                SimpleApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            SimpleApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }

    fun getUserProfile(token: String): UserProfileResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(profileBaseUrl).openConnection() as HttpURLConnection).apply {
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
                    val trimmed = body.trimStart()
                    val user = if (trimmed.startsWith("{")) {
                        val json = JSONObject(body)
                        val userObj = json.optJSONObject("user") ?: json
                        UserProfile(
                            id = userObj.optLong("id"),
                            name = userObj.optString("name"),
                            email = userObj.optString("email"),
                            role = userObj.optString("role", "USER"),
                            profilePictureUrl = userObj.optString("profilePictureUrl", userObj.optString("picture")),
                            createdAt = userObj.optString("createdAt")
                        )
                    } else null

                    UserProfileResult(true, "Success", user)
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
            connection = (URL(profileBaseUrl).openConnection() as HttpURLConnection).apply {
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
            val role = json.optString("role").takeIf { it.isNotBlank() }
            AuthApiResult(true, message, token, name, email, role)
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
                requests.add(parseRequestItem(array.getJSONObject(i)))
            }

            RequestApiResult(true, "Success", requests)
        } catch (_: Exception) {
            RequestApiResult(false, "Failed to parse request list")
        }
    }

    private fun parseRequestItem(item: JSONObject): RequestItem {
        return RequestItem(
            id = item.optLong("id", 0L),
            equipmentName = item.optString("equipmentName"),
            category = item.optString("category"),
            description = item.optString("description"),
            quantity = item.optInt("quantity", 1),
            requesterName = item.optString("requesterName", item.optString("studentName")),
            requesterEmail = item.optString("requesterEmail"),
            borrowDate = item.optString("borrowDate"),
            returnDate = item.optString("returnDate"),
            studentName = item.optString("studentName"),
            schoolIdNumber = item.optString("schoolIdNumber"),
            yearLevel = item.optString("yearLevel"),
            course = item.optString("course"),
            status = item.optString("status"),
            createdAt = item.optString("createdAt"),
            updatedAt = item.optString("updatedAt")
        )
    }

    private fun parseEquipmentItem(item: JSONObject): EquipmentItem {
        return EquipmentItem(
            id = item.optLong("id", 0L),
            name = item.optString("name"),
            category = item.optString("category"),
            description = item.optString("description"),
            availableQuantity = item.optInt("availableQuantity", 0),
            totalQuantity = item.optInt("totalQuantity", 0),
            location = item.optString("location"),
            condition = item.optString("condition"),
            createdAt = item.optString("createdAt")
        )
    }

    private fun getJsonArrayList(
        url: String,
        token: String,
        parser: (JSONObject) -> RequestItem
    ): RequestApiResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode in 200..299) {
                val array = JSONArray(body)
                val items = mutableListOf<RequestItem>()
                for (i in 0 until array.length()) {
                    items.add(parser(array.getJSONObject(i)))
                }
                RequestApiResult(true, "Success", items)
            } else {
                RequestApiResult(false, parseErrorMessage(body, statusCode))
            }
        } catch (_: Exception) {
            RequestApiResult(false, "Unable to connect to backend. Check server and network.")
        } finally {
            connection?.disconnect()
        }
    }
}
