package com.example.sensordiary.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensordiary.ui.theme.*

@Composable
fun ExportModal(
    onDismiss: () -> Unit,
    monthEmojis: List<String>,
    energyTrendData: List<Int>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(horizontal = 24.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            PosterCard(monthEmojis)
            
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "返回", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        copyToClipboard(context, monthEmojis, energyTrendData)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "复制结果", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, emojis: List<String>, trend: List<Int>) {
    val summary = StringBuilder()
    summary.append("【传感器情绪日历 · 个人情绪周报】\n\n")
    summary.append("本周活跃状态概要：\n")
    
    val activeEmojis = emojis.filter { it.isNotBlank() && it != " " }
    if (activeEmojis.isEmpty()) {
        summary.append("（本周暂无检测记录）\n")
    } else {
        summary.append(activeEmojis.joinToString(" "))
        summary.append("\n\n")
        
        val moodCounts = activeEmojis.groupingBy { it }.eachCount()
        val topMood = moodCounts.maxByOrNull { it.value }?.key
        summary.append("本周关键词：${topMood ?: "稳定"}\n")
        
        val avgEnergy = trend.filter { it > 0 }.average()
        if (!avgEnergy.isNaN()) {
            summary.append("本周平均能量值：${avgEnergy.toInt()}%\n")
            val comment = when {
                avgEnergy > 80 -> "状态极佳，充满动力！"
                avgEnergy > 50 -> "状态平稳，继续保持。"
                else -> "能量略低，记得多休息哦。"
            }
            summary.append("心境评价：$comment\n")
        }
    }
    
    summary.append("\n“你的心境就像一个传感器，只有在安静的时候才能接收到最细微的信号。”")
    
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Sensor Diary Summary", summary.toString())
    clipboard.setPrimaryClip(clip)
    
    Toast.makeText(context, "已成功复制文字总结", Toast.LENGTH_SHORT).show()
}

@Composable
fun PosterCard(emojis: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(45.dp),
        color = Slate50,
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Column {
                Text(
                    text = "Weekly Insight",
                    color = Indigo600,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "个人情绪周报",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate800
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "本周活跃状态",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate200,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Grid of emojis (take 28)
                    val displayEmojis = emojis.take(28)
                    displayEmojis.chunked(7).forEachIndexed { rowIndex, chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            chunk.forEach { emoji ->
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                        if (rowIndex < 3) Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = Slate900
            ) {
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopStart)
                            .offset(x = (-16).dp, y = (-8).dp)
                    )
                    Text(
                        text = "“你的心境就像一个传感器，只有在安静的时候才能接收到最细微的信号。”",
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 16.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "传感器情绪日历 专业版",
                color = Slate300,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
