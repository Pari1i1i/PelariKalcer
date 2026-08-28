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

    // ─────────────────────────────────────────────
    // SOCIAL FEED TIMELINE (POSTS, LIKES, COMMENTS)
    // ─────────────────────────────────────────────

    suspend fun createPost(post: PostItem): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || post.userEmail.isBlank()) return@withContext false
        try {
            val url = URL("$baseUrl/rest/v1/posts")
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
                put("postId", if (post.postId.isBlank()) java.util.UUID.randomUUID().toString() else post.postId)
                put("userEmail", post.userEmail)
                put("username", post.username)
                put("imageUrl", post.imageUrl)
                put("caption", post.caption)
                put("distanceKm", post.distanceKm)
                put("paceMinPerKm", post.paceMinPerKm)
                put("durationSeconds", post.durationSeconds)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "createPost exception", e)
            false
        }
    }

    suspend fun fetchPosts(myEmail: String): List<PostItem> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val url = URL("$baseUrl/rest/v1/posts?select=*&order=createdAt.desc&limit=50")
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
                val posts = mutableListOf<PostItem>()

                // Fetch my likes to check isLikedByMe
                val myLikes = fetchMyLikes(myEmail)
                // Fetch all like counts and comment counts
                val likeCounts = fetchLikeCounts()
                val commentCounts = fetchCommentCounts()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("postId")
                    posts.add(
                        PostItem(
                            postId = id,
                            userEmail = obj.optString("userEmail", ""),
                            username = obj.optString("username", "PelariKalcer"),
                            imageUrl = obj.optString("imageUrl", ""),
                            caption = obj.optString("caption", ""),
                            distanceKm = obj.optDouble("distanceKm", 0.0),
                            paceMinPerKm = obj.optDouble("paceMinPerKm", 0.0),
                            durationSeconds = obj.optInt("durationSeconds", 0),
                            createdAt = obj.optString("createdAt", ""),
                            likeCount = likeCounts[id] ?: 0,
                            commentCount = commentCounts[id] ?: 0,
                            isLikedByMe = myLikes.contains(id)
                        )
                    )
                }
                return@withContext posts
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "fetchPosts exception", e)
        }
        emptyList()
    }

    private suspend fun fetchMyLikes(myEmail: String): Set<String> = withContext(Dispatchers.IO) {
        if (!isConfigured || myEmail.isBlank()) return@withContext emptySet()
        try {
            val me = URLEncoder.encode(myEmail, "UTF-8")
            val url = URL("$baseUrl/rest/v1/post_likes?userEmail=eq.$me&select=postId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val array = JSONArray(conn.inputStream.bufferedReader().readText())
                val set = mutableSetOf<String>()
                for (i in 0 until array.length()) {
                    set.add(array.getJSONObject(i).getString("postId"))
                }
                return@withContext set
            }
        } catch (e: Exception) { e.printStackTrace() }
        emptySet()
    }

    private suspend fun fetchLikeCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyMap()
        try {
            val url = URL("$baseUrl/rest/v1/post_likes?select=postId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val array = JSONArray(conn.inputStream.bufferedReader().readText())
                val map = mutableMapOf<String, Int>()
                for (i in 0 until array.length()) {
                    val pid = array.getJSONObject(i).getString("postId")
                    map[pid] = (map[pid] ?: 0) + 1
                }
                return@withContext map
            }
        } catch (e: Exception) { e.printStackTrace() }
        emptyMap()
    }

    private suspend fun fetchCommentCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyMap()
        try {
            val url = URL("$baseUrl/rest/v1/post_comments?select=postId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val array = JSONArray(conn.inputStream.bufferedReader().readText())
                val map = mutableMapOf<String, Int>()
                for (i in 0 until array.length()) {
                    val pid = array.getJSONObject(i).getString("postId")
                    map[pid] = (map[pid] ?: 0) + 1
                }
                return@withContext map
            }
        } catch (e: Exception) { e.printStackTrace() }
        emptyMap()
    }

    suspend fun toggleLike(postId: String, myEmail: String, currentIsLiked: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || postId.isBlank() || myEmail.isBlank()) return@withContext false
        try {
            if (currentIsLiked) {
                // Delete like
                val pe = URLEncoder.encode(postId, "UTF-8")
                val me = URLEncoder.encode(myEmail, "UTF-8")
                val url = URL("$baseUrl/rest/v1/post_likes?postId=eq.$pe&userEmail=eq.$me")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $anonKey")
                }
                conn.responseCode in 200..299
            } else {
                // Insert like
                val url = URL("$baseUrl/rest/v1/post_likes")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $anonKey")
                    setRequestProperty("Prefer", "resolution=merge-duplicates")
                    doOutput = true
                }
                val body = JSONObject().apply {
                    put("postId", postId)
                    put("userEmail", myEmail)
                }
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                conn.responseCode in 200..299
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "toggleLike exception", e)
            false
        }
    }

    suspend fun fetchComments(postId: String): List<CommentItem> = withContext(Dispatchers.IO) {
        if (!isConfigured || postId.isBlank()) return@withContext emptyList()
        try {
            val pe = URLEncoder.encode(postId, "UTF-8")
            val url = URL("$baseUrl/rest/v1/post_comments?postId=eq.$pe&select=*&order=createdAt.asc")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val array = JSONArray(conn.inputStream.bufferedReader().readText())
                val list = mutableListOf<CommentItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CommentItem(
                            commentId = obj.optString("commentId", ""),
                            postId = obj.optString("postId", ""),
                            userEmail = obj.optString("userEmail", ""),
                            username = obj.optString("username", "Pelari"),
                            text = obj.optString("text", ""),
                            createdAt = obj.optString("createdAt", "")
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) { e.printStackTrace() }
        emptyList()
    }

    suspend fun addComment(postId: String, myEmail: String, username: String, text: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || postId.isBlank() || myEmail.isBlank() || text.isBlank()) return@withContext false
        try {
            val url = URL("$baseUrl/rest/v1/post_comments")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                doOutput = true
            }
            val body = JSONObject().apply {
                put("commentId", java.util.UUID.randomUUID().toString())
                put("postId", postId)
                put("userEmail", myEmail)
                put("username", username)
                put("text", text)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "addComment exception", e)
            false
        }
    }
}

data class PostItem(
    val postId: String = java.util.UUID.randomUUID().toString(),
    val userEmail: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val distanceKm: Double = 0.0,
    val paceMinPerKm: Double = 0.0,
    val durationSeconds: Int = 0,
    val createdAt: String = "",
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var isLikedByMe: Boolean = false
)

data class CommentItem(
    val commentId: String = java.util.UUID.randomUUID().toString(),
    val postId: String = "",
    val userEmail: String = "",
    val username: String = "",
    val text: String = "",
    val createdAt: String = ""
)

