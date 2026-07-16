package com.loadcast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.loadcast.app.data.RecordStore
import com.loadcast.app.ui.LoadcastApp
import com.loadcast.app.ui.theme.LoadcastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val store = remember { RecordStore.create(applicationContext) }
            LoadcastTheme {
                LoadcastApp(store)
            }
        }
    }
}
