package com.example.coursework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.coursework.ui.navigation.AppNavHost
import com.example.coursework.ui.theme.RunTrackerTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display, allowing safeDrawingPadding to work
        enableEdgeToEdge()

        setContent {
            RunTrackerTheme {
                AppNavHost()
            }
        }
    }
}