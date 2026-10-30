package com.example.clockdiff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.clockdiff.data.repository.ClockDiffRepositoryImpl
import com.example.clockdiff.ui.navigation.ClockDiffApp
import com.example.clockdiff.ui.theme.ClockDiffTheme
import com.example.clockdiff.ui.viewmodel.ClockDiffViewModel

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        ClockDiffRepositoryImpl(applicationContext)
    }

    private val viewModel: ClockDiffViewModel by viewModels {
        ClockDiffViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            ClockDiffTheme(themeMode = uiState.settings.themeMode) {
                ClockDiffApp(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}
