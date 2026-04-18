package com.example.sensordiary.viewmodel

import android.Manifest
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensordiary.data.AppDatabase
import com.example.sensordiary.model.MoodOption
import com.example.sensordiary.model.MoodRecord
import com.example.sensordiary.util.SensorHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorHelper = SensorHelper(application)
    private val db = AppDatabase.getDatabase(application)
    private val moodDao = db.moodDao()

    // Permission Trigger
    private val _requestPermissionEvent = MutableSharedFlow<Unit>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    var hasAudioPermission by mutableStateOf(false)
        private set

    var isLightSensorSupported by mutableStateOf(true)
        private set
    var isGyroSensorSupported by mutableStateOf(true)
        private set

    // App Share Link
    private val appShareLink = "https://share.feijipan.com/s/7n3kQLEg"

    // UI Navigation State
    var currentTab by mutableStateOf("home")
        private set

    // Sensor Data (Real)
    var lightIntensity by mutableStateOf(0)
        private set
    var ambientDecibels by mutableStateOf(0)
        private set
    var shakeFrequency by mutableStateOf(0f)
        private set
    var realTimeScanFrequency by mutableStateOf(100)
        private set
    private var currentEnergyScore by mutableStateOf(0.5f)

    // Records (Now from DB)
    val moodRecords = mutableStateListOf<MoodRecord>()

    // Analysis Page Data (Derived from DB)
    var energyTrendData by mutableStateOf(listOf<Int>(0, 0, 0, 0, 0, 0, 0))
        private set
    var monthEmojis by mutableStateOf(List(31) { " " })
        private set

    init {
        checkPermissions()
        isLightSensorSupported = sensorHelper.isLightSensorSupported
        isGyroSensorSupported = sensorHelper.isGyroSensorSupported
        
        viewModelScope.launch {
            sensorHelper.lightIntensity.collectLatest {
                lightIntensity = it.toInt()
                delay(5000) // Throttle to 5 seconds
            }
        }
        viewModelScope.launch {
            sensorHelper.ambientDecibels.collectLatest {
                ambientDecibels = it
                delay(5000) // Throttle to 5 seconds
            }
        }
        viewModelScope.launch {
            sensorHelper.shakeFrequency.collectLatest {
                shakeFrequency = it
            }
        }
        viewModelScope.launch {
            sensorHelper.realTimeMagnitude.collectLatest {
                realTimeScanFrequency = it.toInt()
            }
        }
        // Observe records from DB
        viewModelScope.launch {
            moodDao.getAllRecords().collectLatest {
                moodRecords.clear()
                moodRecords.addAll(it)
                updateAnalysisData(it)
            }
        }
    }

    fun checkPermissions() {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestAudioPermission() {
        viewModelScope.launch {
            _requestPermissionEvent.emit(Unit)
        }
    }

    private fun updateAnalysisData(records: List<MoodRecord>) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // 1. Group by day and get latest record per day
        val latestRecordPerDay = records.groupBy { record ->
            calendar.timeInMillis = record.timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        }.mapValues { it.value.maxByOrNull { r -> r.timestamp }!! }

        // 2. Weekly Trend (Last 7 days)
        val weeklyTrend = mutableListOf<Int>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
            val record = latestRecordPerDay[dayKey]
            val score = record?.energyScore ?: 0.5f
            weeklyTrend.add((score * 100).toInt())
        }
        energyTrendData = weeklyTrend

        // 3. Monthly Grid (Current month)
        val monthlyList = mutableListOf<String>()
        for (i in 1..31) {
            val dayKey = "$currentYear-$currentMonth-$i"
            monthlyList.add(latestRecordPerDay[dayKey]?.emoji ?: " ")
        }
        monthEmojis = monthlyList
    }

    fun startMonitoring() {
        sensorHelper.startMonitoring()
    }

    fun stopMonitoring() {
        sensorHelper.stopMonitoring()
    }

    // Date Label
    val dateLabel: String
        get() {
            val sdf = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)
            return sdf.format(Date())
        }

    // Scanning State
    var isScanning by mutableStateOf(false)
        private set
    var scanCountdown by mutableStateOf(3)
        private set
    private var scanJob: Job? = null

    // Result Modal State
    var showResultModal by mutableStateOf(false)
        private set
    var detectedMood by mutableStateOf<MoodOption?>(null)
        private set



    // Export Modal State
    var showExportModal by mutableStateOf(false)
        private set

    // Clear Cache Confirm Dialog State
    var showClearConfirmDialog by mutableStateOf(false)
        private set

    // Delete Single Record Confirm Dialog State
    var showDeleteConfirmDialog by mutableStateOf(false)
        private set
    var recordToDelete by mutableStateOf<MoodRecord?>(null)
        private set

    fun switchTab(tab: String) {
        currentTab = tab
    }

    fun startScanning() {
        if (isScanning) return
        isScanning = true
        scanCountdown = 3
        scanJob = viewModelScope.launch {
            while (scanCountdown > 0) {
                delay(1000)
                scanCountdown--
            }
            finishScanning()
        }
    }

    fun cancelScanning() {
        if (!isScanning) return
        scanJob?.cancel()
        isScanning = false
        scanCountdown = 3
    }

    private fun finishScanning() {
        isScanning = false
        
        // Energy Algorithm (Multi-variable) with fallback for unsupported sensors
        val normalizedShake = if (isGyroSensorSupported) (shakeFrequency / 5f).coerceIn(0f, 1f) else 0.5f
        val normalizedLight = if (isLightSensorSupported) (lightIntensity / 1000f).coerceIn(0f, 1f) else 0.5f
        val normalizedSound = if (hasAudioPermission) (ambientDecibels / 100f).coerceIn(0f, 1f) else 0.5f
        
        // Calculate Energy Score (0 to 1) - Balance weights if sensors missing
        currentEnergyScore = (normalizedLight * 0.4f + (1 - normalizedShake) * 0.4f + (1 - normalizedSound) * 0.2f)

        val moods = listOf(
            MoodOption("😊", "心情愉悦", "基于当前环境光照与心率反馈，您的状态非常积极。"),
            MoodOption("😌", "平和冷静", "基于当前环境光照与心率反馈，建议您尝试深呼吸以保持状态。"),
            MoodOption("😐", "情绪一般", "系统检测到您的生理基准稳定，环境影响较小。"),
            MoodOption("😔", "感到压力", "传感器捕捉到轻微波动，建议适当放松，听听轻音乐。")
        )
        
        detectedMood = when {
            currentEnergyScore > 0.8f -> moods[0]
            currentEnergyScore > 0.5f -> moods[1]
            currentEnergyScore > 0.3f -> moods[2]
            else -> moods[3]
        }
        showResultModal = true
    }

    fun handleResult(save: Boolean) {
        if (save && detectedMood != null) {
            val record = MoodRecord(
                emoji = detectedMood!!.emoji,
                title = detectedMood!!.title,
                description = "采样成功 · 传感器数据已存入",
                timestamp = System.currentTimeMillis(),
                energyScore = currentEnergyScore
            )
            viewModelScope.launch {
                moodDao.insertRecord(record)
            }
        }
        showResultModal = false
        detectedMood = null
    }

    fun toggleExportModal(show: Boolean) {
        showExportModal = show
    }

    fun toggleClearConfirmDialog(show: Boolean) {
        showClearConfirmDialog = show
    }

    fun toggleDeleteConfirmDialog(show: Boolean, record: MoodRecord? = null) {
        recordToDelete = record
        showDeleteConfirmDialog = show
    }

    fun confirmDeleteRecord() {
        recordToDelete?.let {
            viewModelScope.launch {
                moodDao.deleteRecord(it)
                showDeleteConfirmDialog = false
                recordToDelete = null
            }
        }
    }

    fun deleteRecord(record: MoodRecord) {
        viewModelScope.launch {
            moodDao.deleteRecord(record)
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            moodDao.deleteAllRecords()
            showClearConfirmDialog = false
        }
    }

    fun copyResultToClipboard() {
        detectedMood?.let { mood ->
            val content = "【情绪检测结果】\n状态：${mood.emoji} ${mood.title}\n描述：${mood.description}\n检测时间：${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n下载传感器日记 App：$appShareLink"
            copyToClipboard(content, "结果已复制到剪贴板")
        }
    }

    fun copyAnalysisToClipboard() {
        val avgEnergy = if (energyTrendData.isNotEmpty()) energyTrendData.average().toInt() else 0
        val content = "【情绪周报】\n本周平均能量：$avgEnergy%\n检测记录数：${moodRecords.size}\n\n【不写日记】\n一起记录情绪变化，快速下载App最新版本：$appShareLink"
        copyToClipboard(content, "分析报告已复制到剪贴板")
    }

    private fun copyToClipboard(text: String, toastMsg: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SensorDiary Share", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), toastMsg, Toast.LENGTH_SHORT).show()
    }
}
