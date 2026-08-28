package com.example.pelarikalcer.ui.screens.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pelarikalcer.data.local.dao.AiCoachDao
import com.example.pelarikalcer.data.local.entity.AiCoachMessageEntity
import com.example.pelarikalcer.data.local.entity.AiCoachSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(
    val id: Int = 0,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiCoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val sessionId: Int? = null,
    val error: String? = null
)

private const val SYSTEM_PROMPT = """Kamu adalah PelariCoach, pelatih lari virtual di aplikasi PelariKalcer.
Tugasmu membantu pelari dengan memberikan tips lari, saran nutrisi, jadwal latihan, dan motivasi.
Selalu jawab secara langsung dalam Bahasa Indonesia yang ramah, santai, dan positif."""

class AiCoachViewModel(
    private val aiCoachDao: AiCoachDao,
    private val userId: Int,
    private val apiKey: String
) : ViewModel() {

    companion object {
        // Urutan prioritas model. Dicek satu-satu ke daftar model yang benar-benar
        // tersedia buat API key ini (lewat fetchAvailableModels). Google sering
        // "matiin" model tertentu buat API key/project baru tanpa histori pemakaian,
        // jadi list ini perlu di-update kalau suatu saat error 404/NOT_FOUND muncul lagi.
        private val PREFERRED_MODELS = listOf(
            "gemini-3.1-flash-lite",
            "gemini-3.5-flash",
            "gemini-flash-latest",
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash"
        )

        // Cache hasil resolusi model supaya tidak nge-hit endpoint ListModels
        // di setiap pesan yang dikirim user.
        @Volatile
        private var resolvedModelCache: String? = null
    }

    private val _state = MutableStateFlow(AiCoachUiState())
    val state: StateFlow<AiCoachUiState> = _state

    // Conversation history for multi-turn context
    private val conversationHistory = mutableListOf<Pair<String, String>>() // role, text
    private var currentSessionId: Int? = null

    init {
        initSession()
    }

    private fun initSession() {
        viewModelScope.launch {
            val existing = aiCoachDao.getLatestSession(userId)
            if (existing != null) {
                currentSessionId = existing.sessionId
                val msgs = aiCoachDao.getMessagesSnapshot(existing.sessionId)
                _state.value = _state.value.copy(
                    sessionId = existing.sessionId,
                    messages = msgs.map { ChatMessage(it.messageId, it.content, it.sender == "USER") }
                )
                // Restore conversation history for context
                msgs.forEach { msg ->
                    conversationHistory.add(
                        if (msg.sender == "USER") "user" to msg.content
                        else "model" to msg.content
                    )
                }
            } else {
                val sessionId = aiCoachDao.insertSession(
                    AiCoachSessionEntity(userId = userId, topic = "Sesi Baru")
                )
                currentSessionId = sessionId.toInt()
                _state.value = _state.value.copy(sessionId = sessionId.toInt())
                addAiMessage("Halo! Saya PelariCoach, asisten lari pribadimu. Ada yang bisa saya bantu hari ini? Tanya saja tentang tips lari, nutrisi, jadwal latihan, atau apapun yang ingin kamu ketahui!")
            }
        }
    }

    fun onInputChange(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isLoading) return

        _state.value = _state.value.copy(inputText = "")
        addUserMessage(text)

        // PENTING: Tambahkan pesan user ke history di sini
        conversationHistory.add("user" to text)

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val rawAiText = callGeminiRestApi() // Tidak perlu passing 'text' lagi
                val aiText = cleanAiResponse(rawAiText)

                // Simpan balasan AI ke history
                conversationHistory.add("model" to aiText)
                addAiMessage(aiText)
            } catch (e: Exception) {
                val fallback = getOfflineResponse(text)
                addAiMessage(fallback)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun cleanAiResponse(text: String): String {
        // Jika respon mengandung tag/pola thinking ala model reasoning, buang bagian awalnya
        var cleaned = text.trim()

        // Menghapus tanda kutip luar jika AI membungkus seluruh respon dengan quote
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length > 2) {
            cleaned = cleaned.substring(1, cleaned.length - 1).trim()
        }

        return cleaned
    }

    /**
     * Dynamically queries Google API for all models supported by this specific API key.
     */
    private suspend fun fetchAvailableModels(): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val text = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(text)
                val modelsArray = json.optJSONArray("models")
                val result = mutableListOf<String>()
                if (modelsArray != null) {
                    for (i in 0 until modelsArray.length()) {
                        val m = modelsArray.getJSONObject(i)
                        val name = m.getString("name") // e.g. "models/gemini-2.5-flash"
                        val methods = m.optJSONArray("supportedGenerationMethods")
                        var supportsGenerate = false
                        if (methods != null) {
                            for (j in 0 until methods.length()) {
                                if (methods.getString(j) == "generateContent") {
                                    supportsGenerate = true
                                    break
                                }
                            }
                        }
                        if (supportsGenerate) {
                            result.add(name.removePrefix("models/"))
                        }
                    }
                }
                if (result.isNotEmpty()) return@withContext result
            }
        } catch (e: Exception) {
            // Ignore error and use hardcoded fallback list
        }
        return@withContext listOf(
            "gemini-3.1-flash-lite",
            "gemini-3.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash"
        )
    }

    /**
     * Resolve model name yang benar-benar tersedia buat API key ini, dengan urutan
     * prioritas PREFERRED_MODELS. Hasilnya di-cache biar tidak fetch ulang tiap pesan.
     */
    private suspend fun resolveModelName(): String {
        resolvedModelCache?.let { return it }
        val available = fetchAvailableModels()
        val chosen = PREFERRED_MODELS.firstOrNull { it in available }
            ?: available.firstOrNull()
            ?: PREFERRED_MODELS.first()
        resolvedModelCache = chosen
        return chosen
    }

    /**
     * Direct REST call to Gemini v1beta API with dynamic model discovery and fallback.
     */
    private suspend fun callGeminiRestApi(): String = withContext(Dispatchers.IO) {
        val modelName = resolveModelName()
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000

        val contentsArray = JSONArray()

        // Ambil history
        val recentHistory = conversationHistory.takeLast(10)
        recentHistory.forEach { (role, content) ->
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", content)))
            })
        }

        // Pindah System Instruction langsung ke dalam parts atau format v1beta yang benar
        val sysInstructionJson = JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().put(
                "text",
                "$SYSTEM_PROMPT\n\nSANGAT PENTING: Langsung jawab pertanyaan user dalam Bahasa Indonesia. DILARANG menuliskan analisis, rencana jawaban, outline, atau Bahasa Inggris!"
            )))
        }

        val body = JSONObject().apply {
            put("systemInstruction", sysInstructionJson)
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                // Sedikit dilebihkan dari 800, karena token "thinking" (kalau ada)
                // ikut motong budget maxOutputTokens dan bisa bikin respon kepotong/kosong.
                put("maxOutputTokens", 1024)
                put("thinkingConfig", JSONObject().apply {
                    // Gemini 3.x pakai "thinkingLevel" (gak bisa full off, minimal "low").
                    // Gemini 2.5.x pakai "thinkingBudget" (bisa di-set 0 buat full off).
                    if (modelName.startsWith("gemini-3")) {
                        put("thinkingLevel", "low")
                    } else {
                        put("thinkingBudget", 0)
                    }
                })
            })
        }

        connection.outputStream.use { it.write(body.toString().toByteArray()) }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(responseText)
            val candidates = json.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")

            // Gabungkan semua part teks, TAPI skip part yang ditandai "thought": true
            // (bagian reasoning internal model, bukan jawaban final buat user).
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val isThought = part.optBoolean("thought", false)
                if (isThought) continue
                if (part.has("text")) {
                    sb.append(part.getString("text"))
                }
            }

            val result = sb.toString().trim()
            if (result.isBlank()) {
                throw Exception("Respon AI kosong (kemungkinan token habis buat thinking, coba lagi)")
            }
            return@withContext result
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
            // Kalau model yang dipilih ternyata ditolak (404/NOT_FOUND), reset cache
            // supaya percobaan berikutnya nyoba resolve ulang, bukan nyangkut di model mati.
            if (responseCode == 404) {
                resolvedModelCache = null
            }
            throw Exception("API Error $responseCode: $errorText")
        }
    }

    private fun addUserMessage(text: String) {
        val msg = ChatMessage(content = text, isUser = true)
        _state.value = _state.value.copy(messages = _state.value.messages + msg)
        viewModelScope.launch {
            currentSessionId?.let { sid ->
                aiCoachDao.insertMessage(
                    AiCoachMessageEntity(sessionId = sid, sender = "USER", content = text)
                )
            }
        }
    }

    private fun addAiMessage(text: String) {
        val msg = ChatMessage(content = text, isUser = false)
        _state.value = _state.value.copy(messages = _state.value.messages + msg)
        viewModelScope.launch {
            currentSessionId?.let { sid ->
                aiCoachDao.insertMessage(
                    AiCoachMessageEntity(sessionId = sid, sender = "AI", content = text)
                )
            }
        }
    }

    private fun getOfflineResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("cedera") || lower.contains("sakit") ->
                "Untuk mencegah cedera, pastikan kamu selalu melakukan pemanasan 5-10 menit sebelum berlari dan pendinginan setelahnya. Jika sudah cedera, terapkan metode RICE: Rest (istirahat), Ice (kompres es), Compression (perban), Elevation (angkat bagian cedera)."
            lower.contains("pace") || lower.contains("kecepatan") ->
                "Pace ideal untuk pemula adalah 6-8 menit/km. Fokus dulu pada konsistensi jarak, baru tingkatkan kecepatan secara bertahap. Aturan 10%: jangan tambah jarak lebih dari 10% per minggu."
            lower.contains("makan") || lower.contains("nutrisi") || lower.contains("makanan") ->
                "Untuk lari jarak < 10km, makan 1-2 jam sebelum berlari cukup. Pilih karbohidrat kompleks seperti nasi atau roti gandum. Setelah lari, konsumsi protein dalam 30 menit untuk pemulihan otot."
            lower.contains("jadwal") || lower.contains("latihan") ->
                "Untuk pemula, coba jadwal 3x seminggu dengan jeda hari. Contoh: Senin-Rabu-Jumat. Mulai dari 2-3 km, tingkatkan 0.5 km setiap minggu. Pastikan ada hari istirahat untuk pemulihan otot."
            lower.contains("motivasi") || lower.contains("semangat") || lower.contains("malas") ->
                "Ingat alasanmu mulai berlari! Setiap langkah kecil tetap berarti. Jangan bandingkan dirimu dengan orang lain, bandingkan dengan dirimu kemarin."
            else ->
                "Maaf, saya tidak bisa terhubung ke AI saat ini. Coba lagi dalam beberapa saat!"
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            val newSessionId = aiCoachDao.insertSession(
                AiCoachSessionEntity(userId = userId, topic = "Sesi Baru")
            )
            currentSessionId = newSessionId.toInt()
            conversationHistory.clear()
            _state.value = AiCoachUiState(sessionId = newSessionId.toInt())
            addAiMessage("Sesi baru dimulai! Ada yang bisa saya bantu?")
        }
    }
}

class AiCoachViewModelFactory(
    private val aiCoachDao: AiCoachDao,
    private val userId: Int,
    private val apiKey: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiCoachViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiCoachViewModel(aiCoachDao, userId, apiKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}