package com.fitflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.fitflow.ui.components.BottomNavbar
import com.fitflow.ui.screens.DashboardScreen
import com.fitflow.ui.theme.FitFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitFlowTheme {
                Scaffold(
                    bottomBar = { BottomNavbar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        DashboardScreen()
                    }
                }
            }
        }
    }
}
