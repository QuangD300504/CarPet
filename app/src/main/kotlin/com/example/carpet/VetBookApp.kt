package com.example.carpet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.carpet.presentation.navigation.VetBookNavGraph
import com.example.carpet.presentation.theme.VetBookTheme

@Composable
fun VetBookApp() {
    VetBookTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            VetBookNavGraph()
        }
    }
}
