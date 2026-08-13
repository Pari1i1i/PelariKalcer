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
            && !baseUrl.contains("xyzcompany")

    // ─────────────────────────────────────────────
    // USER SYNC
    // ─────────────────────────────────────────────

    suspend fun upsertUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || user.email.isBlank()) return@withContext false
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
                put("email", user.email)
                put("username", user.username)
                put("totalPoints", user.totalPoints)
                put("currentStreak", user.currentStreak)
                put("weightKg", user.weightKg)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "upsertUser exception", e)
            false
        }
    }

    // ─────────────────────────────────────────────
    // SEARCH USERS
    // ─────────────────────────────────────────────

    suspend fun searchUsers(query: String, currentUserEmail: String): List<UserEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured || query.isBlank()) return@withContext emptyList()
        try {
            val q = URLEncoder.encode(query.trim(), "UTF-8")
            val excludeEmail = URLEncoder.encode(currentUserEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/users?username=ilike.%25${q}%25&email=neq.${excludeEmail}&select=*&limit=20")
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
                            userId = 0,
                            username = obj.getString("username"),
                            email = obj.getString("email"),
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
            android.util.Log.e("Supabase", "searchUsers exception", e)
        }
        emptyList()
    }

    // ─────────────────────────────────────────────
    // FRIEND REQUESTS & FRIENDS WORKFLOW
    // ─────────────────────────────────────────────

    /**
     * Send a friend request from `fromEmail` to `toEmail` (status = pending)
     */
    suspend fun sendFriendRequest(fromEmail: String, toEmail: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || fromEmail.isBlank() || toEmail.isBlank()) return@withContext false
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
                put("userEmail", fromEmail)
                put("friendEmail", toEmail)
                put("status", "pending")
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "sendFriendRequest exception", e)
            false
        }
    }

    /**
     * Fetch pending friend requests sent TO `myEmail`
     */
    suspend fun fetchPendingRequests(myEmail: String): List<UserEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured || myEmail.isBlank()) return@withContext emptyList()
        try {
            val me = URLEncoder.encode(myEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/friends?friendEmail=eq.$me&status=eq.pending&select=userEmail")
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
                val senderEmails = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    senderEmails.add(array.getJSONObject(i).getString("userEmail"))
                }

                if (senderEmails.isEmpty()) return@withContext emptyList()

                // Fetch details of sender users
                val inClause = senderEmails.joinToString(",") { URLEncoder.encode(it, "UTF-8") }
                val usersUrl = URL("$baseUrl/rest/v1/users?email=in.($inClause)&select=*")
                val usersConn = (usersUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $anonKey")
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                if (usersConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val usersJson = usersConn.inputStream.bufferedReader().readText()
                    val usersArray = JSONArray(usersJson)
                    val list = mutableListOf<UserEntity>()
                    for (i in 0 until usersArray.length()) {
                        val obj = usersArray.getJSONObject(i)
                        list.add(
                            UserEntity(
                                userId = 0,
                                username = obj.getString("username"),
                                email = obj.getString("email"),
                                passwordHash = "",
                                totalPoints = obj.optInt("totalPoints", 0),
                                currentStreak = obj.optInt("currentStreak", 0),
                                weightKg = obj.optDouble("weightKg", 65.0)
                            )
                        )
                    }
                    return@withContext list
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "fetchPendingRequests exception", e)
        }
        emptyList()
    }

    /**
     * Fetch set of emails where `myEmail` sent a pending request
     */
    suspend fun fetchSentRequestEmails(myEmail: String): Set<String> = withContext(Dispatchers.IO) {
        if (!isConfigured || myEmail.isBlank()) return@withContext emptySet()
        try {
            val me = URLEncoder.encode(myEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/friends?userEmail=eq.$me&status=eq.pending&select=friendEmail")
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
                val set = mutableSetOf<String>()
                for (i in 0 until array.length()) {
                    set.add(array.getJSONObject(i).getString("friendEmail"))
                }
                return@withContext set
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "fetchSentRequestEmails exception", e)
        }
        emptySet()
    }

    /**
     * Accept a pending request: update status = accepted for (sender, me) and (me, sender)
     */
    suspend fun acceptFriendRequest(myEmail: String, senderEmail: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            // Update sender -> me to accepted
            val se = URLEncoder.encode(senderEmail, "UTF-8")
            val me = URLEncoder.encode(myEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/friends?userEmail=eq.$se&friendEmail=eq.$me")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            conn.outputStream.use { it.write(JSONObject().put("status", "accepted").toString().toByteArray()) }
            conn.responseCode in 200..299

            // Also insert reciprocal me -> sender accepted
            val url2 = URL("$baseUrl/rest/v1/friends")
            val conn2 = (url2.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val body2 = JSONObject().apply {
                put("userEmail", myEmail)
                put("friendEmail", senderEmail)
                put("status", "accepted")
            }
            conn2.outputStream.use { it.write(body2.toString().toByteArray()) }
            conn2.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "acceptFriendRequest exception", e)
            false
        }
    }

    /**
     * Reject or cancel friend request
     */
    suspend fun removeFriend(userEmail: String, friendEmail: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val ue = URLEncoder.encode(userEmail, "UTF-8")
            val fe = URLEncoder.encode(friendEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/friends?or=(and(userEmail.eq.$ue,friendEmail.eq.$fe),and(userEmail.eq.$fe,friendEmail.eq.$ue))")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "removeFriend exception", e)
            false
        }
    }

    /**
     * Fetch accepted friends list for `myEmail` from Supabase
     */
    suspend fun fetchAcceptedFriends(myEmail: String): List<UserEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured || myEmail.isBlank()) return@withContext emptyList()
        try {
            val me = URLEncoder.encode(myEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/friends?userEmail=eq.$me&status=eq.accepted&select=friendEmail")
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
                val friendEmails = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    friendEmails.add(array.getJSONObject(i).getString("friendEmail"))
                }
                if (friendEmails.isEmpty()) return@withContext emptyList()

                val inClause = friendEmails.joinToString(",") { URLEncoder.encode(it, "UTF-8") }
                val usersUrl = URL("$baseUrl/rest/v1/users?email=in.($inClause)&select=*&order=totalPoints.desc")
                val usersConn = (usersUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $anonKey")
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                if (usersConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val usersJson = usersConn.inputStream.bufferedReader().readText()
                    val usersArray = JSONArray(usersJson)
                    val list = mutableListOf<UserEntity>()
                    for (i in 0 until usersArray.length()) {
                        val obj = usersArray.getJSONObject(i)
                        list.add(
                            UserEntity(
                                userId = 0,
                                username = obj.getString("username"),
                                email = obj.getString("email"),
                                passwordHash = "",
                                totalPoints = obj.optInt("totalPoints", 0),
                                currentStreak = obj.optInt("currentStreak", 0),
                                weightKg = obj.optDouble("weightKg", 65.0)
                            )
                        )
                    }
                    return@withContext list
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "fetchAcceptedFriends exception", e)
        }
        emptyList()
    }

    // ─────────────────────────────────────────────
    // GLOBAL LEADERBOARD
    // ─────────────────────────────────────────────

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
                            userId = i + 1000,
                            username = obj.getString("username"),
                            email = obj.getString("email"),
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
            android.util.Log.e("Supabase", "fetchGlobalLeaderboard exception", e)
        }
        emptyList()
    }
}
