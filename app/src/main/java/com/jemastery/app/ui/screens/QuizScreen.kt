package com.jemastery.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jemastery.app.data.HistoryItem
import com.jemastery.app.data.Lang
import com.jemastery.app.data.QuizMode
import com.jemastery.app.data.QuizParser
import com.jemastery.app.data.Repo
import com.jemastery.app.ui.components.ProgressBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Green = Color(0xFF10B981)
private val Red = Color(0xFFEF4444)

@Composable
fun QuizScreen(onExit: () -> Unit, onFinished: () -> Unit) {

    val quiz = Repo.activeQuiz
    val questions = Repo.activeQuestions
    val mode = Repo.activeMode
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val practice = mode == QuizMode.PRACTICE

    if (quiz == null || questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Quiz could not be loaded")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onExit) { Text("Go back") }
            }
        }
        return
    }

    var index by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var marked by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var secondsLeft by remember { mutableIntStateOf(quiz.timeMinutes * 60) }
    var showSubmit by remember { mutableStateOf(false) }
    var showPalette by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var lang by remember { mutableStateOf(Lang.ENGLISH) }
    var fontScale by remember { mutableStateOf(1.0f) }
    var autoOn by remember { mutableStateOf(true) }
    var langMenu by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }

    fun finish() {
        if (finished) return
        finished = true
        val total = questions.size
        val correct = questions.indices.count {
            answers[it] == questions[it].correct && questions[it].correct >= 0
        }
        val skipped = total - answers.size
        val wrong = answers.size - correct
        val accuracy = if (total == 0) 0 else (correct * 100) / total
        val used = quiz.timeMinutes * 60 - secondsLeft
        val timeStr = (used / 60).toString() + "m " + (used % 60).toString() + "s"
        val wrongQs = questions.indices.filter { answers[it] != null && answers[it] != questions[it].correct }
        val markedQs = marked.toList().sorted()

        val item = HistoryItem(
            quizId = quiz.id,
            quizTitle = quiz.title,
            correct = correct,
            wrong = wrong,
            skipped = skipped,
            total = total,
            accuracy = accuracy,
            timeTaken = timeStr,
            date = System.currentTimeMillis(),
            wrongQs = wrongQs,
            markedQs = markedQs
        )
        Repo.lastResult = item
        Repo.lastAnswers = answers
        scope.launch { runCatching { Repo.saveHistory(item) } }
        onFinished()
    }

    val finishRef by rememberUpdatedState { finish() }

    LaunchedEffect(mode) {
        if (practice) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
        finishRef()
    }

    LaunchedEffect(index) { revealed = false }

    LaunchedEffect(index, revealed, autoOn) {
        if (!practice || !autoOn || !revealed) return@LaunchedEffect
        delay(2400L)
        if (index < questions.size - 1) index += 1
    }

    BackHandler {
        if (answers.isEmpty()) onExit() else showExit = true
    }

    val q = questions[index]
    val (qText, qHindi) = QuizParser.lang(q.text, lang)
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeText = (if (minutes < 10) "0" else "") + minutes + ":" + (if (seconds < 10) "0" else "") + seconds
    val picked = answers[index]

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    quiz.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (practice) "PRACTICE MODE \u00B7 instant answer" else "EXAM MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (practice) Green else Red
                )
            }
            if (!practice) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (secondsLeft < 60) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        timeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (secondsLeft < 60) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (practice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Text(
                        lang.label + " \u25BE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { langMenu = true }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                    DropdownMenu(expanded = langMenu, onDismissRequest = { langMenu = false }) {
                        Lang.values().forEach { l ->
                            DropdownMenuItem(
                                text = { Text(l.label) },
                                onClick = { lang = l; langMenu = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    "A-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { fontScale = (fontScale - 0.1f).coerceAtLeast(0.8f) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "A+",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { fontScale = (fontScale + 0.1f).coerceAtMost(1.6f) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    if (autoOn) "\u26A1 Auto: ON" else "\u23F8 Auto: OFF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (autoOn) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (autoOn) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { autoOn = !autoOn }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "\uD83D\uDEA9",
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            copyToClipboard(context, "Question " + (index + 1) + ": " + qText)
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/IS1343"))
                                )
                            }
                        }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                )
            }
        }

        ProgressBar(
            fraction = (index + 1).toFloat() / questions.size,
            color = MaterialTheme.colorScheme.primary,
            track = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Question " + (index + 1) + " of " + questions.size,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "\u26A0\uFE0F Report",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Red,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFEF2F2))
                        .clickable {
                            copyToClipboard(context, "Question " + (index + 1) + ": " + qText)
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/IS1343"))
                                )
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            val tag = q.pyqTag
            if (tag != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    tag.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (practice) Color(0xFF3B82F6) else Red,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (practice) Color(0xFFEFF6FF) else Color(0xFFFEF2F2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                qText,
                fontSize = (16 * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = (24 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (qHindi != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    qHindi,
                    fontSize = (15 * fontScale).sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0D9488)
                )
            }

            Spacer(Modifier.height(16.dp))

            q.options.forEachIndexed { i, option ->
                val selected = picked == i
                val isCorrectOpt = i == q.correct
                val showResult = practice && revealed

                val bgColor = when {
                    showResult && isCorrectOpt -> Green.copy(alpha = 0.14f)
                    showResult && selected && !isCorrectOpt -> Red.copy(alpha = 0.14f)
                    selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    showResult && isCorrectOpt -> Green
                    showResult && selected && !isCorrectOpt -> Red
                    selected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val (optEn, optHi) = QuizParser.lang(option, lang)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(
                            width = if (selected || (showResult && isCorrectOpt)) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !(practice && revealed)) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            answers = answers.toMutableMap().apply { put(index, i) }
                            if (practice) revealed = true
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    showResult && isCorrectOpt -> Green
                                    showResult && selected && !isCorrectOpt -> Red
                                    selected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (showResult && isCorrectOpt) "\u2713"
                            else if (showResult && selected && !isCorrectOpt) "\u2715"
                            else ('A' + i).toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected || (showResult && isCorrectOpt)) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            optEn,
                            fontSize = (14 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (optHi != null) {
                            Text(
                                optHi,
                                fontSize = (13 * fontScale).sp,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }
                }
            }

            if (practice) {
                AnimatedVisibility(
                    visible = revealed,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column {
                        val gotIt = picked != null && picked == q.correct
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (gotIt) Green.copy(alpha = 0.14f) else Red.copy(alpha = 0.14f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (gotIt) "\u2705 Correct!" else "\u274C Wrong",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gotIt) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                            Spacer(Modifier.weight(1f))
                            if (!gotIt && q.correct >= 0) {
                                Text(
                                    "Answer: " + ('A' + q.correct),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                        if (q.explanation.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "\uD83D\uDCA1 Explanation",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    q.explanation,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (!practice) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { marked = if (marked.contains(index)) marked - index else marked + index },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (marked.contains(index)) "Unmark review" else "Mark for review", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showPalette = true },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("\uD83D\uDCCB Q-List", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (index < questions.size - 1) index += 1 else showSubmit = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (index == questions.size - 1) "Submit \uD83D\uDE80" else "Next / Skip \u2B07",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (practice) {
                        Text(
                            if (autoOn) "Auto scrolling On" else "Manual Next Mode",
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }

    if (showSubmit) {
        AlertDialog(
            onDismissRequest = { showSubmit = false },
            title = { Text("Submit test?") },
            text = {
                Text(
                    "Answered: " + answers.size + " of " + questions.size +
                        "\nMarked: " + marked.size +
                        "\nUnanswered questions are counted as skipped."
                )
            },
            confirmButton = { Button(onClick = { showSubmit = false; finish() }) { Text("Submit") } },
            dismissButton = { TextButton(onClick = { showSubmit = false }) { Text("Cancel") } }
        )
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Exit quiz?") },
            text = { Text("Your answers for this attempt will be lost.") },
            confirmButton = { Button(onClick = { showExit = false; onExit() }) { Text("Exit") } },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Stay") } }
        )
    }

    if (showPalette) {
        AlertDialog(
            onDismissRequest = { showPalette = false },
            title = { Text("Question list") },
            text = {
                Column(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
                    questions.indices.chunked(5).forEach { rowIdx ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowIdx.forEach { i ->
                                val answered = answers.containsKey(i)
                                val isMarked = marked.contains(i)
                                val bg = when {
                                    isMarked -> MaterialTheme.colorScheme.secondary
                                    answered -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(bg)
                                        .clickable { index = i; showPalette = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        (i + 1).toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (answered || isMarked) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPalette = false; showSubmit = true }) { Text("Submit test") }
            }
        )
    }
}

fun buildAiPrompt(q: com.jemastery.app.data.Question): String {
    val opts = q.options.mapIndexed { i, o -> ('A' + i).toString() + ". " + o }.joinToString("\n")
    val correct = if (q.correct >= 0) ('A' + q.correct).toString() + ". " + q.options.getOrNull(q.correct) else "Not provided"
    return "Act as an expert engineering exam tutor. Explain this competitive exam question step-by-step:\n\n" +
        "Question: " + q.text + "\n\nOptions:\n" + opts + "\n\nCorrect Answer: " + correct +
        "\n\nProvide an accurate, exam-oriented conceptual explanation in clear Hinglish. " +
        "Explain why the correct option is right and other options are incorrect."
}

fun copyToClipboard(context: Context, text: String) {
    runCatching {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("JE Mastery", text))
    }
}
