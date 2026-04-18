package com.example.sensordiary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sensordiary.model.MoodRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<MoodRecord>>

    @Insert
    suspend fun insertRecord(record: MoodRecord)

    @androidx.room.Delete
    suspend fun deleteRecord(record: MoodRecord)

    @Query("DELETE FROM mood_records")
    suspend fun deleteAllRecords()
}
