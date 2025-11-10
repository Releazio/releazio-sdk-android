package com.releazio.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.releazio.example.ui.MainContent
import com.releazio.example.ui.theme.ReleazioExampleTheme

/**
 * Main Activity for Releazio Example
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    var isDarkTheme by remember { mutableStateOf(false) }
    
    ReleazioExampleTheme(darkTheme = isDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainContent(
                modifier = Modifier.padding(innerPadding),
                isDarkTheme = isDarkTheme,
                onThemeChange = { isDarkTheme = it }
            )
        }
    }
}
