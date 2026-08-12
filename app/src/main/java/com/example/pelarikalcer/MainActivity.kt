package com.example.pelarikalcer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pelarikalcer.ui.navigation.AppNavigation
import com.example.pelarikalcer.ui.theme.PelariKalcerTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Properly initialize osmdroid with SharedPreferences and User-Agent
        // This MUST happen before any MapView is created
        val osmPrefs = applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        val osmConfig = Configuration.getInstance()
        osmConfig.load(applicationContext, osmPrefs)
        osmConfig.userAgentValue = "${packageName}/1.0 PelariKalcerApp"
        osmConfig.osmdroidBasePath = cacheDir
        osmConfig.osmdroidTileCache = java.io.File(cacheDir, "osmdroid")

        enableEdgeToEdge()
        setContent {
            PelariKalcerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}


