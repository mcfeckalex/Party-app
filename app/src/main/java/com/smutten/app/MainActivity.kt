package com.smutten.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smutten.app.navigation.SmuttenNavHost
import com.smutten.app.ui.theme.SmuttenTheme
import com.smutten.app.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmuttenTheme {
                val gameViewModel: GameViewModel = viewModel()
                SmuttenNavHost(gameViewModel = gameViewModel)
            }
        }
    }
}
