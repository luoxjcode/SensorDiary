package com.example.sensordiary.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensordiary.ui.theme.*

@Composable
fun BottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent) // Transparent for edge-to-edge
            .padding(horizontal = 24.dp, vertical = 12.dp) // Reduced horizontal padding to move icons outward
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Icon
            Column(
                modifier = Modifier.weight(1f).pointerInput(Unit) {
                    detectTapGestures { onTabSelected("home") }
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentTab == "home") Indigo600 else Slate300,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "日记",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentTab == "home") Indigo600 else Slate300
                )
            }

            Spacer(modifier = Modifier.width(120.dp)) // Increased spacer to push icons apart

            // Analysis Icon
            Column(
                modifier = Modifier.weight(1f).pointerInput(Unit) {
                    detectTapGestures { onTabSelected("analysis") }
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Analysis",
                    tint = if (currentTab == "analysis") Indigo600 else Slate300,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "分析",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentTab == "analysis") Indigo600 else Slate300
                )
            }
        }

        // Center Scan Button
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp)
        ) {
            PulseRing()
            ScanButton(onStartScan = onStartScan, onCancelScan = onCancelScan)
        }
    }
}

@Composable
fun ScanButton(onStartScan: () -> Unit, onCancelScan: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f, label = "scale")

    Surface(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onStartScan()
                        tryAwaitRelease()
                        isPressed = false
                        onCancelScan()
                    }
                )
            },
        shape = CircleShape,
        color = Slate900,
        border = androidx.compose.foundation.BorderStroke(6.dp, Color.White),
        shadowElevation = 32.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Scan",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun PulseRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .background(Indigo600.copy(alpha = alpha), CircleShape)
    )
}
