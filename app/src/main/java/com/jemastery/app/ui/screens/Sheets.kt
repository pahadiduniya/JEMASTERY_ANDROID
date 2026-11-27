package com.jemastery.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import com.jemastery.app.data.LeaderEntry
import com.jemastery.app.data.Repo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jemastery.app.data.Access
import com.jemastery.app.data.Quiz
import com.jemastery.app.ui.components.BottomSheet

@Composable
fun SectionSheet(
    quiz: Quiz,
    selected: List<Int>,
    onToggle: (Int) -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    BottomSheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("\uD83E\uDDE9 Select Sections", fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("\u2716", fontSize = 18.sp, modifier = Modifier.clickable { onDismiss() })
        }
        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .height(320.dp)
                .verticalScroll(rememberScrollState())
        ) {
            quiz.sections.forEachIndexed { idx, sec ->
                val checked = selected.contains(idx)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onToggle(idx) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sec.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Q." + sec.start + " to Q." + sec.end + " (" + sec.count + " Qs)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(checked = checked, onCheckedChange = { onToggle(idx) })
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue \uD83D\uDC49", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ModeSheet(
    quiz: Quiz,
    onDismiss: () -> Unit,
    onStart: (practice: Boolean) -> Unit
) {
    BottomSheet(onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                quiz.title,
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2
            )
            Text("\u2716", fontSize = 18.sp, modifier = Modifier.clickable { onDismiss() })
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Choose how you want to attempt this test",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        if (quiz.levelRating > 0 || quiz.contentRating > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 14.dp)
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quiz Level", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        String.format("%.1f", quiz.levelRating) + " \u2B50",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Content Quality", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        String.format("%.1f", quiz.contentRating) + " \u2B50",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { onStart(false) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Text("\u23F3 Start Exam Mode", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onStart(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("\uD83D\uDCDD Start Practice Mode", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun PaymentSheet(
    title: String,
    required: Access,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    BottomSheet(onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83D\uDD12 Unlock PRO+", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
            Spacer(Modifier.weight(1f))
            Text("\u2716", fontSize = 18.sp, modifier = Modifier.clickable { onDismiss() })
        }
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlanCard("1 Month", "\u20B929", false, Modifier.weight(1f))
            PlanCard("3 Months", "\u20B979", true, Modifier.weight(1f))
            PlanCard("6 Months", "\u20B9149", false, Modifier.weight(1f))
        }

        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(14.dp)
        ) {
            Text("1. Pay via UPI and take a screenshot.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("2. Send the screenshot and your registered email to the admin on Telegram.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/IS1343")))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29A9EB))
        ) {
            Text("\uD83D\uDCAC Message Admin (@IS1343)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PlanCard(name: String, price: String, popular: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (popular) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (popular) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            price,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            color = if (popular) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
        )
        if (popular) {
            Spacer(Modifier.height(3.dp))
            Text("POPULAR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
        }
    }
}


@Composable
fun LeaderboardSheet(quiz: Quiz, onDismiss: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var entries by remember { mutableStateOf<List<LeaderEntry>>(emptyList()) }

    LaunchedEffect(quiz.id) {
        entries = runCatching { Repo.loadLeaderboard(quiz.id) }.getOrDefault(emptyList())
        loading = false
    }

    BottomSheet(onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83C\uDFC6 Leaderboard", fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("\u2716", fontSize = 18.sp, modifier = Modifier.clickable { onDismiss() })
        }
        Spacer(Modifier.height(4.dp))
        Text(
            quiz.title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
        Spacer(Modifier.height(14.dp))

        if (loading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (entries.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No attempts yet. Be the first! \uD83D\uDE80",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .height(320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                entries.take(10).forEachIndexed { idx, e ->
                    val medal = when (idx) {
                        0 -> "\uD83E\uDD47"
                        1 -> "\uD83E\uDD48"
                        2 -> "\uD83E\uDD49"
                        else -> (idx + 1).toString()
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (e.isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(medal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            e.name + (if (e.isMe) " (you)" else ""),
                            fontSize = 13.sp,
                            fontWeight = if (e.isMe) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            e.score.toString() + "/" + e.total,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}
