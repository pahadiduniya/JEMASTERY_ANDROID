package com.jemastery.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun ProgressBar(
    fraction: Float,
    color: Color,
    track: Color,
    modifier: Modifier = Modifier,
    height: Int = 8
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(50))
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun GoogleG(size: Int = 22) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val stroke = this.size.minDimension * 0.22f
        val r = (this.size.minDimension - stroke) / 2f
        val topLeft = Offset((this.size.width - 2 * r) / 2f, (this.size.height - 2 * r) / 2f)
        val arcSize = Size(2 * r, 2 * r)
        fun arc(color: Color, start: Float, sweep: Float) {
            drawArc(
                color = color,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }
        arc(Color(0xFFEA4335), 200f, 80f)
        arc(Color(0xFFFBBC05), 110f, 90f)
        arc(Color(0xFF34A853), 20f, 90f)
        arc(Color(0xFF4285F4), -60f, 80f)
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(this.size.width / 2f, this.size.height / 2f - stroke / 2f),
            size = Size(this.size.width / 2f, stroke)
        )
    }
}

@Composable
fun RemoteIcon(url: String, size: Int, fallback: String = "\uD83D\uDCC2", blurRadius: Int = 0) {
    var bmp by remember(url) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url) {
        if (!url.startsWith("http")) return@LaunchedEffect
        bmp = withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val bytes = conn.inputStream.use { it.readBytes() }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
        }
    }

    val image = bmp
    val mod = Modifier
        .size(size.dp)
        .clip(RoundedCornerShape(8.dp))
        .let { if (blurRadius > 0) it.blur(blurRadius.dp) else it }

    if (image != null) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = mod
        )
    } else {
        Text(fallback, fontSize = (size * 0.7f).sp)
    }
}

@Composable
fun BottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
                .clickable(enabled = false) {}
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) {}
                    .padding(20.dp)
            ) {
                Column { content() }
            }
        }
    }
}
