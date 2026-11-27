package com.jemastery.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jemastery.app.data.Access
import com.jemastery.app.data.Folder
import com.jemastery.app.data.Quiz
import com.jemastery.app.data.Repo
import com.jemastery.app.data.Tier
import com.jemastery.app.data.canOpen
import com.jemastery.app.ui.components.RemoteIcon
import com.jemastery.app.ui.theme.ThemeState
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onOpenProfile: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var path by remember { mutableStateOf(listOf(Folder("root", "Library", "\uD83C\uDFE0", "", "free"))) }
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    // sheets
    var lockedQuiz by remember { mutableStateOf<Quiz?>(null) }
    var lockedFolder by remember { mutableStateOf<Folder?>(null) }
    var sectionQuiz by remember { mutableStateOf<Quiz?>(null) }
    var modeQuiz by remember { mutableStateOf<Quiz?>(null) }
    var pendingSections by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showPayment by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            runCatching { Repo.loadProfile() }
            runCatching { Repo.loadOffer() }
            Repo.loadLibrary()
        } catch (e: Exception) {
            error = e.message ?: "Could not load library"
        }
        loading = false
    }

    val tier = Repo.tier
    BackHandler(enabled = path.size > 1) { path = path.dropLast(1) }

    val current = path.last()
    val q = query.trim().lowercase()

    val subFolders = Repo.folders
        .filter { it.parentId == current.id }
        .filter { q.isEmpty() || it.name.lowercase().contains(q) }
        .sortedBy { it.name.lowercase() }

    val subQuizzes = Repo.quizzes
        .filter { it.folderId == current.id }
        .filter { q.isEmpty() || it.title.lowercase().contains(q) }
        .sortedWith(compareBy({ it.sequence }, { it.title.lowercase() }))

    fun openQuiz(quiz: Quiz) {
        val access = Access.of(quiz.accessType, false)
        if (!canOpen(access, tier)) {
            lockedQuiz = quiz
            return
        }
        if (quiz.isMultiSection) {
            pendingSections = quiz.sections.indices.toList()
            sectionQuiz = quiz
        } else {
            pendingSections = emptyList()
            modeQuiz = quiz
        }
    }

    fun startSelected(quiz: Quiz, sections: List<Int>, practice: Boolean) {
        scope.launch {
            busy = true
            try {
                val qs = Repo.loadQuestions(quiz, sections)
                if (qs.isEmpty()) {
                    error = "This quiz has no questions yet."
                } else {
                    Repo.activeQuiz = quiz
                    Repo.activeSections = sections
                    Repo.activeQuestions = qs
                    Repo.activeMode = if (practice) com.jemastery.app.data.QuizMode.PRACTICE
                    else com.jemastery.app.data.QuizMode.EXAM
                    onStartQuiz()
                }
            } catch (e: Exception) {
                error = e.message ?: "Could not open quiz"
            }
            busy = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ---- top bar ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.jemastery.app.R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "JE MASTERY 360\u00B0",
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (ThemeState.dark) "\u2600\uFE0F" else "\uD83C\uDF19",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clickable { ThemeState.toggleDark(context) }
                )
                val pillColor = when (tier) {
                    Tier.PRO_PLUS -> Color(0xFF10B981)
                    Tier.PRO -> Color(0xFFF59E0B)
                    Tier.FREE -> Color(0xFF64748B)
                }
                Text(
                    if (tier == Tier.PRO_PLUS) "PRO+ \u2B50" else tier.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(pillColor)
                        .clickable { onOpenProfile() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
            ) {
                Spacer(Modifier.height(10.dp))

                // ---- search ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                "Search quizzes, topics, PYQs or folders",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ---- breadcrumb ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    path.forEachIndexed { i, f ->
                        if (i > 0) Text("  /  ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (i == 0) "\uD83C\uDFE0 " + f.name else f.name,
                            fontSize = 13.sp,
                            fontWeight = if (i == path.size - 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (i == path.size - 1) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                if (i < path.size - 1) path = path.subList(0, i + 1)
                            }
                        )
                    }
                }

                if (Repo.offerTitle.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF7ED))
                            .padding(12.dp)
                    ) {
                        Text("\uD83D\uDD25 ", fontSize = 13.sp)
                        Text(
                            Repo.offerTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (error != null && Repo.folders.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Connection error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(6.dp))
                            Text(error!!, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    if (subFolders.isEmpty() && subQuizzes.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "\uD83D\uDCC2 Nothing here yet.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ---- folders ----
                    subFolders.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { folder ->
                                val access = Access.of(folder.accessType, false)
                                val locked = !canOpen(access, tier)
                                FolderCard(
                                    folder = folder,
                                    locked = locked,
                                    chip = if (locked) access.chip else null,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (locked) {
                                            lockedFolder = folder
                                        } else {
                                            path = path + folder
                                        }
                                    }
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // ---- quizzes ----
                    subQuizzes.forEach { quiz ->
                        val access = Access.of(quiz.accessType, false)
                        val locked = !canOpen(access, tier)
                        QuizCard(
                            quiz = quiz,
                            access = access,
                            locked = locked,
                            onClick = { openQuiz(quiz) },
                            onLocked = { lockedQuiz = quiz }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(30.dp))
            }
        }

        if (busy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading quiz...", fontSize = 12.sp)
                }
            }
        }
    }

    // ---- sheets ----
    val sq = sectionQuiz
    if (sq != null) {
        SectionSheet(
            quiz = sq,
            selected = pendingSections,
            onToggle = { idx ->
                pendingSections = if (pendingSections.contains(idx)) pendingSections - idx
                else pendingSections + idx
            },
            onDismiss = { sectionQuiz = null },
            onContinue = {
                if (pendingSections.isEmpty()) {
                    error = "Select at least one section"
                } else {
                    sectionQuiz = null
                    modeQuiz = sq
                }
            }
        )
    }

    val mq = modeQuiz
    if (mq != null) {
        ModeSheet(
            quiz = mq,
            onDismiss = { modeQuiz = null },
            onStart = { practice ->
                modeQuiz = null
                startSelected(mq, pendingSections, practice)
            }
        )
    }

    val lq = lockedQuiz
    if (lq != null || lockedFolder != null || showPayment) {
        PaymentSheet(
            title = lq?.title ?: lockedFolder?.name ?: "PRO+ Content",
            required = Access.of(lq?.accessType ?: lockedFolder?.accessType ?: "pro_only", false),
            onDismiss = {
                lockedQuiz = null
                lockedFolder = null
                showPayment = false
            }
        )
    }
}

@Composable
private fun FolderCard(
    folder: Folder,
    locked: Boolean,
    chip: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.let { if (locked) it.blur(6.dp) else it },
                    contentAlignment = Alignment.Center
                ) {
                    if (folder.icon.startsWith("http")) {
                        RemoteIcon(url = folder.icon, size = 52)
                    } else {
                        Text(folder.icon, fontSize = 40.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    folder.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.let { if (locked) it.blur(5.dp) else it }
                )
            }
            if (locked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) { Text("\uD83D\uDD12", fontSize = 15.sp) }

                if (chip != null) {
                    Text(
                        chip,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFEF4444))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizCard(
    quiz: Quiz,
    access: Access,
    locked: Boolean,
    onClick: () -> Unit,
    onLocked: () -> Unit
) {
    val chipColor = when (access) {
        Access.FREE -> Color(0xFF10B981)
        Access.PRO -> Color(0xFFF59E0B)
        Access.PRO_PLUS -> Color(0xFFEF4444)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (locked) onLocked() else onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    access.chip,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = chipColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(chipColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "\u23F1 " + quiz.timeMinutes + " Mins",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                if (quiz.isMultiSection) {
                    Text(
                        "\uD83E\uDDE9 " + quiz.sections.size,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEC4899)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quiz.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.let { if (locked) it.blur(4.dp) else it }
                    )
                    if (quiz.totalQs > 0) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            quiz.totalQs.toString() + " Questions",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.size(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (locked) Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFEF4444)))
                            else Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        if (locked) "\uD83D\uDD12 Locked" else "Start Now",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("\uD83C\uDFC6 Leaderboard", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("\uD83D\uDCCA Attempts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("\uD83D\uDD17 Share", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
