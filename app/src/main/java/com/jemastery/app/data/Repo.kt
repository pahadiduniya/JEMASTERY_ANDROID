package com.jemastery.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jemastery.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Repo {

    private val base: String
        get() = "https://firestore.googleapis.com/v1/projects/" + BuildConfig.PROJECT_ID +
            "/databases/(default)/documents"

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val user: FirebaseUser? get() = auth.currentUser
    val isGuest: Boolean get() = user?.isAnonymous ?: true

    var folders: List<Folder> = emptyList()
        private set
    var quizzes: List<Quiz> = emptyList()
        private set
    var profile: UserProfile = UserProfile()
        private set

    var activeQuiz: Quiz? = null
    var activeSections: List<Int> = emptyList()
    var activeQuestions: List<Question> = emptyList()
    var activeMode: QuizMode = QuizMode.EXAM
    var lastResult: HistoryItem? = null
    var lastAnswers: Map<Int, Int> = emptyMap()
    var offerTitle: String = ""
    var offerBody: String = ""

    val tier: Tier get() = profile.tier

    // ---------- http ----------

    private suspend fun idToken(): String? {
        val u = auth.currentUser ?: return null
        return runCatching { u.getIdToken(false).await().token }.getOrNull()
    }

    private suspend fun call(method: String, path: String, body: JSONObject? = null): JSONObject? =
        withContext(Dispatchers.IO) {
            val token = idToken() ?: return@withContext null
            var conn: HttpURLConnection? = null
            try {
                conn = URL(base + "/" + path).openConnection() as HttpURLConnection
                conn.requestMethod = method
                conn.connectTimeout = 15000
                conn.readTimeout = 25000
                conn.setRequestProperty("Authorization", "Bearer " + token)
                conn.setRequestProperty("Content-Type", "application/json")
                if (body != null) {
                    conn.doOutput = true
                    conn.outputStream.use { it.write(body.toString().toByteArray()) }
                }
                val code = conn.responseCode
                if (code < 200 || code > 299) return@withContext null
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                JSONObject(text)
            } catch (e: Exception) {
                null
            } finally {
                conn?.disconnect()
            }
        }

    private suspend fun callRaw(method: String, path: String, body: JSONObject? = null): String? =
        withContext(Dispatchers.IO) {
            val token = idToken() ?: return@withContext null
            var conn: HttpURLConnection? = null
            try {
                conn = URL(base + "/" + path).openConnection() as HttpURLConnection
                conn.requestMethod = method
                conn.connectTimeout = 15000
                conn.readTimeout = 25000
                conn.setRequestProperty("Authorization", "Bearer " + token)
                conn.setRequestProperty("Content-Type", "application/json")
                if (body != null) {
                    conn.doOutput = true
                    conn.outputStream.use { it.write(body.toString().toByteArray()) }
                }
                val code = conn.responseCode
                if (code < 200 || code > 299) return@withContext null
                conn.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                null
            } finally {
                conn?.disconnect()
            }
        }

    private fun intArray(list: List<Int>): JSONObject {
        val arr = org.json.JSONArray()
        list.forEach { arr.put(JSONObject().put("integerValue", it.toString())) }
        return JSONObject().put("arrayValue", JSONObject().put("values", arr))
    }

    private fun fields(doc: JSONObject): JSONObject = doc.optJSONObject("fields") ?: JSONObject()

    private fun docId(doc: JSONObject): String = doc.optString("name").substringAfterLast('/')

    private fun str(f: JSONObject, key: String): String? {
        val v = f.optJSONObject(key) ?: return null
        val s = v.optString("stringValue")
        return if (s.isEmpty()) null else s
    }

    private fun num(f: JSONObject, key: String): Long? {
        val v = f.optJSONObject(key) ?: return null
        val i = v.optString("integerValue")
        if (i.isNotEmpty()) return i.toLongOrNull()
        val d = v.optString("doubleValue")
        if (d.isNotEmpty()) return d.toDoubleOrNull()?.toLong()
        return null
    }

    private fun dbl(f: JSONObject, key: String): Double {
        val v = f.optJSONObject(key) ?: return 0.0
        val d = v.optString("doubleValue")
        if (d.isNotEmpty()) return d.toDoubleOrNull() ?: 0.0
        val i = v.optString("integerValue")
        if (i.isNotEmpty()) return i.toLongOrNull()?.toDouble() ?: 0.0
        return 0.0
    }

    private fun flag(f: JSONObject, key: String): Boolean =
        f.optJSONObject(key)?.optBoolean("booleanValue", false) ?: false

    private fun arrayValues(f: JSONObject, key: String): List<JSONObject> {
        val v = f.optJSONObject(key) ?: return emptyList()
        val a = v.optJSONObject("arrayValue") ?: return emptyList()
        val arr = a.optJSONArray("values") ?: return emptyList()
        return (0 until arr.length()).map { arr.optJSONObject(it) ?: JSONObject() }
    }

    private fun docs(json: JSONObject?): List<JSONObject> {
        val arr = json?.optJSONArray("documents") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it) }
    }

    private fun sections(f: JSONObject): List<Section> =
        arrayValues(f, "sections").mapNotNull { item ->
            val m = item.optJSONObject("mapValue")?.optJSONObject("fields") ?: return@mapNotNull null
            val name = str(m, "name") ?: return@mapNotNull null
            val start = (num(m, "start") ?: 0L).toInt()
            val end = (num(m, "end") ?: 0L).toInt()
            if (start <= 0 || end < start) null else Section(name, start, end)
        }

    // ---------- reads ----------

    suspend fun loadProfile() {
        val u = auth.currentUser
        if (u == null) {
            profile = UserProfile(isGuest = true)
            return
        }
        val doc = call("GET", "users/" + u.uid)
        if (doc != null) {
            val f = fields(doc)
            profile = UserProfile(
                name = str(f, "name") ?: (u.displayName ?: "student"),
                username = str(f, "username") ?: "",
                email = str(f, "email") ?: (u.email ?: ""),
                isPremium = flag(f, "isPremium"),
                isGuest = u.isAnonymous,
                isBanned = flag(f, "isBanned"),
                proExpiresAt = num(f, "proExpiresAt") ?: 0L
            )
        } else {
            profile = UserProfile(
                name = u.displayName ?: if (u.isAnonymous) "guest" else "student",
                username = "",
                email = u.email ?: "",
                isPremium = false,
                isGuest = u.isAnonymous
            )
        }
    }

    suspend fun loadOffer() {
        val doc = call("GET", "app_settings/main_offer") ?: return
        val f = fields(doc)
        if (flag(f, "isActive")) {
            offerTitle = str(f, "title") ?: "Special offer"
            offerBody = str(f, "message") ?: str(f, "body") ?: ""
        } else {
            offerTitle = ""
            offerBody = ""
        }
    }

    suspend fun loadLibrary() {
        val foldersJson = call("GET", "folders?pageSize=300")
        if (foldersJson == null) throw IllegalStateException("Could not reach the database")
        folders = docs(foldersJson).map { d ->
            val f = fields(d)
            Folder(
                id = docId(d),
                name = str(f, "name") ?: "Untitled",
                icon = str(f, "icon") ?: "\uD83D\uDCC2",
                parentId = str(f, "parentId") ?: "root",
                accessType = str(f, "accessType") ?: "free"
            )
        }

        val quizzesJson = call("GET", "quizzes?pageSize=300")
        quizzes = docs(quizzesJson).map { d ->
            val f = fields(d)
            val isPremium = flag(f, "isPremium")
            val total = num(f, "totalQs") ?: num(f, "totalQuestions") ?: num(f, "questionCount") ?: 0L
            Quiz(
                id = docId(d),
                folderId = str(f, "folderId") ?: "",
                title = str(f, "title") ?: "Untitled Quiz",
                sequence = (num(f, "sequence") ?: 999L).toInt(),
                accessType = str(f, "accessType") ?: if (isPremium) "pro_only" else "free",
                timeMinutes = (num(f, "timeMinutes") ?: 60L).toInt(),
                totalQs = total.toInt(),
                isMigrated = flag(f, "isMigrated"),
                textData = str(f, "textData") ?: "",
                sections = sections(f),
                telegramCode = str(f, "telegramCode") ?: "",
                levelRating = dbl(f, "levelRating"),
                contentRating = dbl(f, "contentRating")
            )
        }
    }

    suspend fun loadQuestions(quiz: Quiz, selectedSections: List<Int> = emptyList()): List<Question> {
        val text = if (quiz.isMigrated) {
            val doc = call("GET", "quiz_contents/" + quiz.id)
            if (doc == null) "" else (str(fields(doc), "textData") ?: "")
        } else {
            quiz.textData
        }
        val all = QuizParser.parse(text)
        return QuizParser.sliceForSections(all, quiz, selectedSections)
    }

    suspend fun saveHistory(item: HistoryItem) {
        val u = auth.currentUser ?: return
        val f = JSONObject()
        f.put("quizId", JSONObject().put("stringValue", item.quizId))
        f.put("quizTitle", JSONObject().put("stringValue", item.quizTitle))
        f.put("correct", JSONObject().put("integerValue", item.correct.toString()))
        f.put("wrong", JSONObject().put("integerValue", item.wrong.toString()))
        f.put("skipped", JSONObject().put("integerValue", item.skipped.toString()))
        f.put("total", JSONObject().put("integerValue", item.total.toString()))
        f.put("accuracy", JSONObject().put("integerValue", item.accuracy.toString()))
        f.put("score", JSONObject().put("integerValue", item.correct.toString()))
        f.put("timeTaken", JSONObject().put("stringValue", item.timeTaken))
        f.put("mode", JSONObject().put("stringValue", "exam"))
        f.put("date", JSONObject().put("integerValue", item.date.toString()))
        f.put("wrongQs", intArray(item.wrongQs))
        f.put("markedQs", intArray(item.markedQs))
        call("POST", "users/" + u.uid + "/history", JSONObject().put("fields", f))
    }

    suspend fun loadHistory(): List<HistoryItem> {
        val u = auth.currentUser ?: return emptyList()
        val json = call("GET", "users/" + u.uid + "/history?pageSize=50") ?: return emptyList()
        return docs(json).map { d ->
            val f = fields(d)
            HistoryItem(
                quizId = str(f, "quizId") ?: "",
                quizTitle = str(f, "quizTitle") ?: "",
                correct = (num(f, "correct") ?: 0L).toInt(),
                wrong = (num(f, "wrong") ?: 0L).toInt(),
                skipped = (num(f, "skipped") ?: 0L).toInt(),
                total = (num(f, "total") ?: 0L).toInt(),
                accuracy = (num(f, "accuracy") ?: 0L).toInt(),
                timeTaken = str(f, "timeTaken") ?: "",
                date = num(f, "date") ?: 0L,
                wrongQs = arrayValues(f, "wrongQs").mapNotNull { it.optString("integerValue").toIntOrNull() },
                markedQs = arrayValues(f, "markedQs").mapNotNull { it.optString("integerValue").toIntOrNull() }
            )
        }.sortedByDescending { it.date }
    }

    suspend fun loadLeaderboard(quizId: String): List<LeaderEntry> {
        val sq = JSONObject()
            .put("from", org.json.JSONArray().put(JSONObject().put("collectionId", "leaderboard")))
            .put(
                "where",
                JSONObject().put(
                    "fieldFilter",
                    JSONObject()
                        .put("field", JSONObject().put("fieldPath", "quizId"))
                        .put("op", "EQUAL")
                        .put("value", JSONObject().put("stringValue", quizId))
                )
            )
            .put("limit", 30)
        val body = JSONObject().put("structuredQuery", sq)
        val raw = callRaw("POST", ":runQuery", body) ?: return emptyList()
        val arr = runCatching { org.json.JSONArray(raw) }.getOrNull() ?: return emptyList()
        val myName = profile.username.ifBlank { profile.name }
        val out = ArrayList<LeaderEntry>()
        for (i in 0 until arr.length()) {
            val doc = arr.optJSONObject(i)?.optJSONObject("document") ?: continue
            val f = fields(doc)
            out.add(
                LeaderEntry(
                    name = str(f, "name") ?: "student",
                    score = (num(f, "score") ?: 0L).toInt(),
                    total = (num(f, "total") ?: 0L).toInt(),
                    timeTakenSec = (num(f, "timeTakenSec") ?: 0L).toInt(),
                    isMe = (str(f, "name") ?: "") == myName
                )
            )
        }
        return out.sortedWith(compareByDescending<LeaderEntry> { it.score }.thenBy { it.timeTakenSec })
    }

    fun signOut() {
        auth.signOut()
        folders = emptyList()
        quizzes = emptyList()
        profile = UserProfile()
        activeQuiz = null
        activeQuestions = emptyList()
        activeSections = emptyList()
        lastResult = null
        lastAnswers = emptyMap()
        offerTitle = ""
        offerBody = ""
    }
}
