package com.jemastery.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
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
import com.jemastery.app.data.HistoryItem
import com.jemastery.app.data.Repo
import com.jemastery.app.data.Tier
import com.jemastery.app.ui.theme.ThemeState
import com.jemastery.app.ui.theme.bgPresets

@Composable
fun ProfileScreen(onBack: () -> Unit, onSignedOut: () -> Unit) {

    val context = LocalContext.current
    var profile by remember { mutableStateOf(Repo.profile) }
    var history by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        runCatching { Repo.loadProfile() }
        profile = Repo.profile
        history = runCatching { Repo.loadHistory() }.getOrDefault(emptyList())
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("\u2716", fontSize = 18.sp, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.weight(1f))
            Text("My Profile", fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(18.dp))
        }

        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                (profile.name.ifBlank { "S" }).take(1).uppercase(),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "@" + (profile.username.ifBlank { profile.name.ifBlank { "student" } }) + " \u270F\uFE0F",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            profile.email.ifBlank { if (profile.isGuest) "Guest session" else "" },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        // tier card
        val tierColor = when (profile.tier) {
            Tier.PRO_PLUS -> Color(0xFF10B981)
            Tier.PRO -> Color(0xFFF59E0B)
            Tier.FREE -> Color(0xFF64748B)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(tierColor.copy(alpha = 0.10f))
                .border(1.dp, tierColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "CURRENT TIER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tierColor
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (profile.tier == Tier.PRO_PLUS) "PRO+ PASS \u2B50" else profile.tier.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(tierColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when (profile.tier) {
                    Tier.PRO_PLUS -> "All Quizzes & Mocks Unlocked"
                    Tier.PRO -> "Free + PRO quizzes unlocked"
                    Tier.FREE -> "Free quizzes only \u00B7 login for more"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (profile.tier == Tier.PRO_PLUS) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "\u23F1 Validity: " + profile.validityDays + " Days Remaining",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tierColor
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                "MY PERFORMANCE DASHBOARD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            DashRow("\uD83D\uDCDD", "My Attempted Quizzes", history.size.toString())
            DashRow("\u274C", "My Wrong Attempts", history.sumOf { it.wrong }.toString())
            DashRow("\uD83D\uDEA9", "My Marked Questions", "-")
        }

        Spacer(Modifier.height(14.dp))

        // theme presets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("BACKGROUND", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                bgPresets.forEachIndexed { i, preset ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(preset.top)
                            .border(
                                width = if (ThemeState.preset == i) 3.dp else 1.dp,
                                color = if (ThemeState.preset == i) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { ThemeState.setPreset(context, i) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { ThemeState.toggleDark(context) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (ThemeState.dark) "\u2600\uFE0F Light mode" else "\uD83C\uDF19 Dark mode", fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/jemastery360")))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) { Text("\uD83D\uDD14 Join Telegram group", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                Repo.signOut()
                onSignedOut()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("\uD83D\uDEAA Log Out Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DashRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 15.sp)
        Spacer(Modifier.size(10.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}
