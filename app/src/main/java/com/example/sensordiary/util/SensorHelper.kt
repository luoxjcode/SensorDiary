package com.example.sensordiary.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log10

class SensorHelper(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val isLightSensorSupported = lightSensor != null
    val isGyroSensorSupported = gyroSensor != null

    private val _lightIntensity = MutableStateFlow(0f)
    val lightIntensity = _lightIntensity.asStateFlow()

    private val _ambientDecibels = MutableStateFlow(0)
    val ambientDecibels = _ambientDecibels.asStateFlow()

    private val _shakeFrequency = MutableStateFlow(0f)
    val shakeFrequency = _shakeFrequency.asStateFlow()

    private val _realTimeMagnitude = MutableStateFlow(0f)
    val realTimeMagnitude = _realTimeMagnitude.asStateFlow()

    private var gyroLastTimestamp = 0L
    private var gyroPeakCount = 0
    private var gyroStartTime = 0L

    private var audioRecord: AudioRecord? = null
    private var isMonitoringAudio = false
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    fun startMonitoring() {
        // Start Light Sensor
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        // Start Gyro Sensor
        gyroSensor?.let {
            gyroStartTime = System.currentTimeMillis()
            gyroPeakCount = 0
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // Start Audio Monitoring if permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startAudioMonitoring()
        }
    }

    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        stopAudioMonitoring()
    }

    @SuppressLint("MissingPermission")
    private fun startAudioMonitoring() {
        if (isMonitoringAudio) return
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                isMonitoringAudio = true
                
                Thread {
                    val buffer = ShortArray(bufferSize)
                    while (isMonitoringAudio) {
                        val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                        if (readSize > 0) {
                            var sum = 0.0
                            for (i in 0 until readSize) {
                                sum += buffer[i] * buffer[i]
                            }
                            val amplitude = sum / readSize
                            val db = if (amplitude > 0) (10 * log10(amplitude)).toInt() else 0
                            _ambientDecibels.value = db
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudioMonitoring() {
        isMonitoringAudio = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            _lightIntensity.value = event.values[0]
        } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            // Calculate rotational velocity magnitude
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
            
            // Real-time magnitude for UI (scaled to look like HZ as per screenshot)
            _realTimeMagnitude.value = 100f + (magnitude * 20f).coerceIn(0f, 100f)
            
            // Peak detection for shake frequency
            if (magnitude > 1.0f) { // Threshold for a "shake"
                val now = System.currentTimeMillis()
                if (now - gyroLastTimestamp > 100) { // Debounce peaks
                    gyroPeakCount++
                    gyroLastTimestamp = now
                    
                    val elapsedSeconds = (now - gyroStartTime) / 1000f
                    if (elapsedSeconds > 0) {
                        _shakeFrequency.value = gyroPeakCount / elapsedSeconds
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
