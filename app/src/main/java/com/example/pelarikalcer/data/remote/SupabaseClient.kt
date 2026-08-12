package com.example.pelarikalcer.data.remote

import com.example.pelarikalcer.BuildConfig
import com.example.pelarikalcer.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SupabaseClient {

    private val baseUrl: String get() = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey: String get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && anonKey.isNotBlank()

    /**
     * Upserts a user to Supabase REST API (creates or updates user profile & points online)
     */
    suspend fun upsertUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val url = URL("$baseUrl/rest/v1/users")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val body = JSONObject().apply {
                put("userId", user.userId)
                put("username", user.username)
                put("email", user.email)
                put("totalPoints", user.totalPoints)
                put("currentStreak", user.currentStreak)
                put("weightKg", user.weightKg)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Searches users across all devices on Supabase by username or email
     */
    suspend fun searchUsers(query: String, currentUserId: Int): List<UserEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured || query.isBlank()) return@withContext emptyList()
        try {
            val encodedQuery = URLEncoder.encode("*${query.trim()}*", "UTF-8")
            val url = URL("$baseUrl/rest/v1/users?username=ilike.$encodedQuery&userId=neq.$currentUserId&select=*&limit=20")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = conn.inputStream.bufferedReader().readText()
                val array = JSONArray(jsonText)
                val list = mutableListOf<UserEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        UserEntity(
                            userId = obj.getInt("userId"),
                            username = obj.getString("username"),
                            email = obj.optString("email", ""),
                            passwordHash = "",
                            totalPoints = obj.optInt("totalPoints", 0),
                            currentStreak = obj.optInt("currentStreak", 0),
                            weightKg = obj.optDouble("weightKg", 65.0)
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    /**
     * Adds a friend relationship online on Supabase
     */
    suspend fun addFriend(userId: Int, friendUserId: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val url = URL("$baseUrl/rest/v1/friends")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val body = JSONObject().apply {
                put("userId", userId)
                put("friendUserId", friendUserId)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Removes a friend relationship online on Supabase
     */
    suspend fun removeFriend(userId: Int, friendUserId: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val url = URL("$baseUrl/rest/v1/friends?userId=eq.$userId&friendUserId=eq.$friendUserId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            conn.responseCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Fetches global leaderboard across all devices from Supabase
     */
    suspend fun fetchGlobalLeaderboard(): List<UserEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val url = URL("$baseUrl/rest/v1/users?select=*&order=totalPoints.desc&limit=50")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = conn.inputStream.bufferedReader().readText()
                val array = JSONArray(jsonText)
                val list = mutableListOf<UserEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        UserEntity(
                            userId = obj.getInt("userId"),
                            username = obj.getString("username"),
                            email = obj.optString("email", ""),
                            passwordHash = "",
                            totalPoints = obj.optInt("totalPoints", 0),
                            currentStreak = obj.optInt("currentStreak", 0),
                            weightKg = obj.optDouble("weightKg", 65.0)
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }
}
