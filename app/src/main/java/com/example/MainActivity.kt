package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.screens.JarvisMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            // Request RECORD_AUDIO permission on launch so Voice Call & Mic work effortlessly
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted or denied handled gracefully */ }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JarvisMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
