package com.example.sensordiary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.sensordiary.ui.components.BottomNavBar
import com.example.sensordiary.ui.components.ExportModal
import com.example.sensordiary.ui.components.ResultModal
import com.example.sensordiary.ui.components.ScanLayer
import com.example.sensordiary.ui.screens.AnalysisScreen
import com.example.sensordiary.ui.screens.HomeScreen
import com.example.sensordiary.ui.theme.ScreenBackground
import com.example.sensordiary.ui.theme.SensorDiaryTheme
import com.example.sensordiary.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        
        // Request permissions
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        
        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 100)
        }

        lifecycleScope.launch {
            viewModel.requestPermissionEvent.collect {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    100
                )
            }
        }

        setContent {
            SensorDiaryTheme {
                MainApp(viewModel)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.checkPermissions()
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Restart monitoring to include audio now that permission is granted
                viewModel.stopMonitoring()
                viewModel.startMonitoring()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
        viewModel.startMonitoring()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopMonitoring()
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = viewModel.currentTab,
                onTabSelected = { viewModel.switchTab(it) },
                onStartScan = { viewModel.startScanning() },
                onCancelScan = { viewModel.cancelScanning() }
            )
        },
        containerColor = ScreenBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (viewModel.currentTab) {
                "home" -> HomeScreen(viewModel)
                "analysis" -> AnalysisScreen(viewModel)
            }
        }
    }

    if (viewModel.isScanning) {
        ScanLayer(
            countdown = viewModel.scanCountdown,
            shakeFrequency = viewModel.realTimeScanFrequency.toFloat()
        )
    }

    if (viewModel.showResultModal) {
        ResultModal(
            mood = viewModel.detectedMood,
            onDismiss = { viewModel.handleResult(false) },
            onSave = { viewModel.handleResult(true) }
        )
    }

    if (viewModel.showExportModal) {
        ExportModal(
            onDismiss = { viewModel.toggleExportModal(false) },
            monthEmojis = viewModel.monthEmojis,
            energyTrendData = viewModel.energyTrendData
        )
    }
}
