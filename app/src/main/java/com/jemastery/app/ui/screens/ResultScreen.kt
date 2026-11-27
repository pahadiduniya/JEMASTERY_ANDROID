package com.jemastery.app.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jemastery.app.data.QuizParser
import com.jemastery.app.data.Repo

@Composable
fun ResultScreen(onHome: () -> Unit, onProfile: () -> Unit) {

    val r = Repo.lastResult
    val questions = Repo.activeQuestions
    val answers = Repo.lastAnswers
    val context = LocalContext.current
    var filter by remember { mutableStateOf("all") }

    BackHandler { onHome() }

    if (r == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No result to show")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onHome) { Text("Home") }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(6.dp))
        Text("Quiz Completed! \uD83C\uDF89", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(
            "Detailed analysis of your performance.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    r.correct.toString(),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Out of " + r.total,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Accuracy: " + r.accuracy + "%",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            StatCard("\u2705", r.correct.toString(), "Correct", Color(0xFF10B981), Modifier.weight(1f))
            StatCard("\u274C", r.wrong.toString(), "Wrong", Color(0xFFEF4444), Modifier.weight(1f))
            StatCard("\u2014", r.skipped.toString(), "Skipped", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
            StatCard("\u23F1", r.timeTaken, "Time", Color(0xFFF59E0B), Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Detailed Solution Review",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all", "correct", "wrong", "skipped").forEach { f ->
                val active = filter == f
                Text(
                    f.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { filter = f }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        questions.forEachIndexed { i, q ->
            val given = answers[i]
            val isCorrect = given != null && given == q.correct
            val passes = when (filter) {
                "correct" -> isCorrect
                "wrong" -> given != null && !isCorrect
                "skipped" -> given == null
                else -> true
            }
            if (!passes) return@forEachIndexed

            val (qText, qHi) = QuizParser.lang(q.text, com.jemastery.app.data.Lang.ENGLISH)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Q" + (i + 1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    if (given == null) {
                        Text("\u2014 Skipped", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (isCorrect) {
                        Text("\u2705 Correct", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    } else {
                        Text("\u274C Wrong", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(qText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
                if (qHi != null) {
                    Text(qHi, fontSize = 11.sp, color = Color(0xFF0D9488))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your answer: " + (given?.let { q.options.getOrNull(it) ?: "-" } ?: "Not attempted"),
                    fontSize = 11.sp,
                    color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                if (!isCorrect && q.correct >= 0) {
                    Text(
                        "Correct: " + (q.options.getOrNull(q.correct) ?: "-"),
                        fontSize = 11.sp,
                        color = Color(0xFF10B981)
                    )
                }
                if (q.explanation.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ex: " + q.explanation,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "\uD83E\uDD16 AI Solution",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF5F3FF))
                            .clickable {
                                val prompt = buildAiPrompt(q)
                                copyToClipboard(context, prompt)
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            android.net.Uri.parse(
                                                "https://www.perplexity.ai/search?q=" + android.net.Uri.encode(prompt)
                                            )
                                        )
                                    )
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("\uD83C\uDFE0 Back to Library", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                val text = "I scored " + r.correct + "/" + r.total + " (" + r.accuracy + "%) in " +
                    r.quizTitle + " on JE Mastery 360! \uD83D\uDD25"
                runCatching {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, "Share result"))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("\uD83D\uDD17 Share result") }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("\uD83D\uDCCA My progress") }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatCard(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 13.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
