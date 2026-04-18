package com.example.sensordiary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_records")
data class MoodRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emoji: String,
    val title: String,
    val description: String,
    val timestamp: Long, // Use Long for full timestamp
    val energyScore: Float = 0.5f // Store the calculated energy score
)

data class MoodOption(
    val emoji: String,
    val title: String,
    val description: String
)
