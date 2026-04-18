package com.example.sensordiary.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensordiary.model.MoodRecord
import com.example.sensordiary.ui.theme.*
import com.example.sensordiary.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var revealedRecordId by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()

    // Collapse revealed item on scroll
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            revealedRecordId = -1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    revealedRecordId = -1
                })
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            item {
                Header(viewModel.dateLabel)
                Spacer(modifier = Modifier.height(40.dp))
                SensorGrid(
                    viewModel.lightIntensity,
                    viewModel.ambientDecibels,
                    viewModel.hasAudioPermission,
                    viewModel.isLightSensorSupported,
                    viewModel.isGyroSensorSupported,
                    onRequestPermission = { viewModel.requestAudioPermission() }
                )
                Spacer(modifier = Modifier.height(40.dp))
                SectionTitle("今日检测记录")
                Spacer(modifier = Modifier.height(24.dp))
            }
            items(viewModel.moodRecords, key = { it.id }) { record ->
                SwipeToRevealDelete(
                    isRevealed = revealedRecordId == record.id,
                    onReveal = { revealedRecordId = record.id },
                    onCollapse = { revealedRecordId = -1 },
                    onDeleteClick = { viewModel.toggleDeleteConfirmDialog(true, record) }
                ) {
                    LogCard(record)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                DisclaimerTip()
                Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
            }
        }

        if (viewModel.showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleDeleteConfirmDialog(false) },
                title = { Text("确认删除", fontWeight = FontWeight.Bold) },
                text = { Text("确定要删除这条检测记录吗？此操作无法撤销。") },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.confirmDeleteRecord()
                            revealedRecordId = -1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("确认删除", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleDeleteConfirmDialog(false) }) {
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
fun SwipeToRevealDelete(
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onCollapse: () -> Unit,
    onDeleteClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val maxRevealPx = with(density) { -80.dp.toPx() }
    var offsetX by remember { mutableStateOf(0f) }

    // Synchronize state with isRevealed
    LaunchedEffect(isRevealed) {
        offsetX = if (isRevealed) maxRevealPx else 0f
    }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
    ) {
        // Background Delete Button
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Red.copy(alpha = 0.8f))
                .clickable { onDeleteClick() }
                .padding(end = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        }

        // Foreground Content
        Surface(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < maxRevealPx / 2) {
                                offsetX = maxRevealPx
                                onReveal()
                            } else {
                                offsetX = 0f
                                onCollapse()
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                            onCollapse()
                        },
                        onHorizontalDrag = { change: PointerInputChange, dragAmount: Float ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(maxRevealPx, 0f)
                        }
                    )
                }
                .pointerInput(isRevealed) {
                    detectTapGestures(onTap = {
                        onCollapse()
                    })
                },
            shape = RoundedCornerShape(32.dp),
            color = Slate50
        ) {
            content()
        }
    }
}

@Composable
fun Header(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Set fixed height to match AnalysisHeader
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "不写日记",
                color = Slate800,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = date,
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SensorGrid(
    light: Int, 
    sound: Int, 
    hasAudioPermission: Boolean, 
    isLightSupported: Boolean,
    isGyroSupported: Boolean,
    onRequestPermission: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SensorCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LightMode,
            iconBg = Amber50,
            iconColor = Amber500,
            value = if (isLightSupported) light.toString() else "不支持",
            label = "光照指数"
        )
        SensorCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.SettingsInputAntenna,
            iconBg = Indigo50,
            iconColor = Indigo600,
            value = if (hasAudioPermission) sound.toString() else "未授权",
            label = "环境分贝",
            showPermissionButton = !hasAudioPermission,
            onPermissionClick = onRequestPermission
        )
    }
}

@Composable
fun SensorCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    value: String,
    label: String,
    showPermissionButton: Boolean = false,
    onPermissionClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(35.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate50),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = iconBg
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (showPermissionButton) {
                Button(
                    onClick = onPermissionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "点击授权",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Indigo600
                    )
                }
            } else {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Slate800)
            }
            
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = Slate300,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp
    )
}

@Composable
fun LogCard(record: MoodRecord) {
    val timeStr = remember(record.timestamp) {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINESE)
        sdf.format(java.util.Date(record.timestamp))
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Slate50
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = record.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)
                Text(text = record.description, fontSize = 9.sp, color = Slate400)
            }
            Text(
                text = timeStr,
                fontSize = 10.sp,
                color = Slate300,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun DisclaimerTip() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "温馨提示",
            color = Slate300,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "情绪分析数据由传感器算法生成，仅供参考，不作为专业诊断依据。",
            color = Slate300.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@Composable
fun Modifier.drawBehindBorder(color: Color, strokeWidth: androidx.compose.ui.unit.Dp): Modifier {
    return this.drawBehind {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, size.height),
            strokeWidth = strokeWidth.toPx()
        )
    }
}
