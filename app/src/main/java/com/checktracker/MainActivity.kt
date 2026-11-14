package com.checktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.checktracker.presentation.main.MainScreen
import com.checktracker.presentation.stats.StatsScreen
import com.checktracker.presentation.theme.CheckTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckTrackerTheme {
                CheckTrackerRoot()
            }
        }
    }
}

// Нижняя навигация между списком чеков и аналитикой
@Composable
private fun CheckTrackerRoot() {
    var showStats by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = !showStats,
                    onClick = { showStats = false },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    label = { Text("Чеки") },
                )
                NavigationBarItem(
                    selected = showStats,
                    onClick = { showStats = true },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Аналитика") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (showStats) StatsScreen() else MainScreen()
        }
    }
}
