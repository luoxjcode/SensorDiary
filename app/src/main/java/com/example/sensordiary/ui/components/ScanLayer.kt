package com.example.sensordiary.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensordiary.ui.theme.*

@Composable
fun ScanLayer(countdown: Int, shakeFrequency: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617).copy(alpha = 0.98f)) // Deepest background
    ) {
        // 1. Primary Focus Group (Centralized)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Countdown Text - Dominant
            Text(
                text = if (countdown > 0) countdown.toString() else "0",
                color = Color.White,
                fontSize = 140.sp, // Slightly larger for dominance
                fontWeight = FontWeight.Thin,
                letterSpacing = (-10).sp,
                modifier = Modifier.offset(y = (-10).dp)
            )
            
            // Animated Bars - Dynamic companion
            Row(
                modifier = Modifier.height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                PulseBar(color = Color.White, duration = 1000)
                BounceBar(color = Indigo500, duration = 800)
                PulseBar(color = Color.White.copy(alpha = 0.3f), duration = 1200)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Analysis Text - Sub-header
            Text(
                text = "正在进行情绪分析",
                color = Indigo400.copy(alpha = 0.8f), // Slightly dimmed
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium, // Not as bold
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(64.dp)) // Increased spacing before tip

            // 2. Secondary Element (Auxiliary Tip)
            val infiniteTransition = rememberInfiniteTransition(label = "hint_flash")
            val hintAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f, // Lower opacity for subtleness
                targetValue = 0.5f, // Dimmer peak
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearOutSlowInEasing), // Slower breath
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hint_alpha"
            )

            Text(
                text = "请拿稳手机，保持自然放松...",
                color = Color.White.copy(alpha = hintAlpha), // Use white but very dim
                fontSize = 11.sp, // Smaller font
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun PulseBar(color: Color, duration: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun BounceBar(color: Color, duration: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val heightScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "height"
    )
    Box(
        modifier = Modifier
            .width(4.dp)
            .fillMaxHeight(heightScale)
            .background(color)
    )
}
