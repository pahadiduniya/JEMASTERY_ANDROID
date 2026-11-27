package com.jemastery.app.data

/** Port of the web app's parseTXT() and processLangText(). */
object QuizParser {

    private val checkMarks = charArrayOf('\u2705', '\u2714', '\u2713')
    private val blockSplit = Regex("\n\\s*\n+")
    private val qNumber = Regex("^[Q]?[0-9]+\\.\\s*", RegexOption.IGNORE_CASE)
    private val tagRegex = Regex("\\[(.*?)]")
    private val stripTag = Regex("\\s*\\[.*?]\\s*")
    private val optPrefix = Regex("^[(]?[A-Ea-e][.)]\\s+")

    private fun isOption(line: String, letter: Char): Boolean {
        val t = line.trimStart()
        if (t.isEmpty()) return false
        val c = t[0]
        if (c == letter || c == letter.lowercaseChar()) return true
        if (c == '(' && t.length > 1) {
            val d = t[1]
            return d == letter || d == letter.lowercaseChar()
        }
        return false
    }

    fun parse(raw: String): List<Question> {
        val text = raw.replace("\r", "").trim()
        if (text.isEmpty()) return emptyList()

        val out = ArrayList<Question>()

        for (block in blockSplit.split(text)) {
            val lines = block.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.size < 3) continue

            val work = lines.toMutableList()
            var explanation = ""
            if (work.isNotEmpty() && work.last().startsWith("ex:", ignoreCase = true)) {
                explanation = work.last().substringAfter(":").trim()
                work.removeAt(work.size - 1)
            }
            val n = work.size
            if (n < 3) continue

            var optStart = (n - 4).coerceAtLeast(1)
            for (i in (n - 1) downTo maxOf(1, n - 6)) {
                if (i + 1 < n && isOption(work[i], 'A') && isOption(work[i + 1], 'B')) {
                    optStart = i
                    break
                }
            }

            var qText = work.subList(0, optStart.coerceAtMost(n)).joinToString("\n")
            qText = qNumber.replace(qText, "")

            val tag = tagRegex.find(qText)?.groupValues?.get(1)?.trim()?.ifEmpty { null }
            val cleanText = stripTag.replace(qText, " ").trim()

            val options = ArrayList<String>()
            var correct = -1

            for (i in optStart until n) {
                var opt = work[i]
                if (opt.any { it in checkMarks }) {
                    correct = options.size
                    opt = opt.trimEnd(*checkMarks).trim()
                }
                opt = optPrefix.replace(opt, "").trim()
                if (opt.isNotEmpty()) options.add(opt)
            }

            if (options.size >= 2) {
                out.add(
                    Question(
                        text = cleanText,
                        options = options,
                        correct = correct,
                        explanation = explanation,
                        pyqTag = tag
                    )
                )
            }
        }
        return out
    }

    /** "English / Hindi" -> picks the part for the chosen language. */
    fun lang(text: String, lang: Lang): Pair<String, String?> {
        if (!text.contains(" / ")) return text to null
        val parts = text.split(" / ")
        val en = parts.first().trim()
        val hi = parts.drop(1).joinToString(" / ").trim()
        return when (lang) {
            Lang.ENGLISH -> en to null
            Lang.HINDI -> hi to null
            Lang.BILINGUAL -> en to hi
        }
    }

    fun sliceForSections(all: List<Question>, quiz: Quiz, selected: List<Int>): List<Question> {
        if (selected.isEmpty() || quiz.sections.isEmpty()) return all
        val out = ArrayList<Question>()
        selected.forEach { idx ->
            val sec = quiz.sections.getOrNull(idx) ?: return@forEach
            val from = (sec.start - 1).coerceIn(0, all.size)
            val to = sec.end.coerceIn(from, all.size)
            out.addAll(all.subList(from, to))
        }
        return if (out.isEmpty()) all else out
    }
}
