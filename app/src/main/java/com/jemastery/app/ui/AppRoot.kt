package com.jemastery.app.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jemastery.app.R
import com.jemastery.app.data.Repo
import com.jemastery.app.ui.screens.LibraryScreen
import com.jemastery.app.ui.screens.LoginScreen
import com.jemastery.app.ui.screens.ProfileScreen
import com.jemastery.app.ui.screens.QuizScreen
import com.jemastery.app.ui.screens.ResultScreen
import com.jemastery.app.ui.theme.ThemeState

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val context = LocalContext.current
    var booted by remember { mutableStateOf(false) }
    var startRoute by remember { mutableStateOf("login") }

    LaunchedEffect(Unit) {
        ThemeState.load(context)
        if (Repo.auth.currentUser != null) {
            runCatching { Repo.loadProfile() }
            startRoute = "library"
        }
        booted = true
    }

    if (!booted) {
        SplashScreen()
        return
    }

    NavHost(navController = nav, startDestination = startRoute) {
        composable("login") {
            LoginScreen(onDone = {
                nav.navigate("library") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("library") {
            LibraryScreen(
                onOpenProfile = { nav.navigate("profile") },
                onStartQuiz = { nav.navigate("quiz") }
            )
        }
        composable("quiz") {
            QuizScreen(
                onExit = { nav.popBackStack() },
                onFinished = { nav.navigate("result") }
            )
        }
        composable("result") {
            ResultScreen(
                onHome = { nav.navigate("library") { popUpTo("library") { inclusive = true } } },
                onProfile = { nav.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { nav.popBackStack() },
                onSignedOut = { nav.navigate("login") { popUpTo(0) } }
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(150.dp)
            )
            Spacer(Modifier.size(20.dp))
            Text(
                "JE MASTERY 360",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
