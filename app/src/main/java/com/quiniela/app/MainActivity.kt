package com.quiniela.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quiniela.app.ui.navigation.QuinielaNavGraph
import com.quiniela.app.ui.theme.QuinielaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuinielaTheme {
                QuinielaNavGraph()
            }
        }
    }
}
