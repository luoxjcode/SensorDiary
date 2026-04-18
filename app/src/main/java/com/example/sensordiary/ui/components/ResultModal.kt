package com.example.sensordiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensordiary.model.MoodOption
import com.example.sensordiary.ui.theme.*

@Composable
fun ResultModal(
    mood: MoodOption?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (mood == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900.copy(alpha = 0.6f))
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(45.dp),
            color = Color.White,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    Text(text = mood.emoji, fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = mood.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mood.description,
                        fontSize = 11.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
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
                            colors = ButtonDefaults.buttonColors(containerColor = Slate50),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text(text = "忽略", color = Slate400, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = onSave,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text(
                                text = "保存记录",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
