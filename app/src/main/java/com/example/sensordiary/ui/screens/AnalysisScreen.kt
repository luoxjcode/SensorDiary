package com.example.sensordiary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensordiary.ui.theme.*
import com.example.sensordiary.viewmodel.MainViewModel

@Composable
fun AnalysisScreen(viewModel: MainViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            item {
                AnalysisHeader(
                    onShare = { viewModel.copyAnalysisToClipboard() },
                    onClearCache = { viewModel.toggleClearConfirmDialog(true) }
                )
                Spacer(modifier = Modifier.height(40.dp))
                MonthGridCard(viewModel.monthEmojis)
                Spacer(modifier = Modifier.height(32.dp))
                TrendChartCard(viewModel.energyTrendData)
            }
        }

        if (viewModel.showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleClearConfirmDialog(false) },
                title = {
                    Text(
                        text = "清理本地数据",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Slate800
                    )
                },
                text = {
                    Text(
                        text = "是否确认清理所有检测记录？此操作不可撤销。",
                        fontSize = 14.sp,
                        color = Slate400
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearAllRecords() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("确认清理", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleClearConfirmDialog(false) }) {
                        Text("取消", color = Slate400, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun AnalysisHeader(onShare: () -> Unit, onClearCache: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "深度洞察",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Slate800,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "多维传感器情绪报告",
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Minimalist Clear Cache Button (Icon only)
            IconButton(
                onClick = onClearCache,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Cache",
                    tint = Color.Red.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
            // Minimalist Share Button (Icon only)
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Indigo600,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TrendChartCard(data: List<Int>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate50),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 16.dp)
                        .background(Indigo600, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "本周能量趋势图",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate800,
                    letterSpacing = (-0.5).sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            EnergyLineChart(data)
            Spacer(modifier = Modifier.height(16.dp))
            val dayLabels = remember {
                val sdf = java.text.SimpleDateFormat("E", java.util.Locale.CHINESE)
                val cal = java.util.Calendar.getInstance()
                List(7) { i ->
                    val c = java.util.Calendar.getInstance()
                    c.add(java.util.Calendar.DAY_OF_YEAR, -(6 - i))
                    sdf.format(c.time)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                }
            }
        }
    }
}

@Composable
fun EnergyLineChart(data: List<Int>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)
        val maxVal = 100f
        
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                // Use cubic curve for tension 0.4 effect
                val prevX = (index - 1) * stepX
                val prevY = height - (data[index - 1] / maxVal * height)
                val controlX1 = prevX + stepX / 2
                val controlY1 = prevY
                val controlX2 = prevX + stepX / 2
                val controlY2 = y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }
        
        drawPath(
            path = path,
            color = Indigo600,
            style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun MonthGridCard(emojis: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp), // Slightly smaller corner radius
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate50),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) { // Reduced outer padding
            Text(
                text = "情绪日历",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Slate300,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp)) // Reduced title spacer
            
            val chunks = emojis.chunked(7)
            chunks.forEachIndexed { rowIndex, chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    chunk.forEachIndexed { colIndex, emoji ->
                        val day = rowIndex * 7 + colIndex + 1
                        if (day <= 31) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(32.dp)
                            ) {
                                Text(
                                    text = day.toString(), 
                                    fontSize = 7.sp, 
                                    color = Slate300,
                                    modifier = Modifier.offset(y = 4.dp) // Tighter to emoji
                                )
                                Text(
                                    text = emoji.ifEmpty { "•" }, 
                                    fontSize = 16.sp,
                                    color = if (emoji.isEmpty()) Slate200 else Slate800
                                )
                            }
                        } else {
                            // Empty box for alignment
                            Box(modifier = Modifier.width(32.dp))
                        }
                    }
                }
                if (rowIndex < chunks.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp)) // Significantly reduced row spacing
                }
            }
        }
    }
}
