package com.example.miniprojet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miniprojet.ui.navigation.AppNavGraph
import com.example.miniprojet.ui.theme.MiniProjetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MiniProjetTheme {
                AppNavGraph()
            }
        }
    }
}