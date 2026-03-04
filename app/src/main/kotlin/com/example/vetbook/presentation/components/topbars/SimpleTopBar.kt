package com.example.vetbook.presentation.components.topbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Type-B header used by all sub/drill-down screens.
 *
 * Layout:
 *   [ ← back ]   [ title centred ]   [ trailingContent? ]
 *   [ searchBar? — full width below title row             ]
 *
 * @param title            Screen title, displayed centred.
 * @param onBackClick      Called when the back arrow is tapped.
 * @param trailingContent  Optional trailing icon/action (e.g. list/map toggle).
 * @param searchBar        Optional composable placed below the title row (e.g. OutlinedTextField).
 */
@Composable
fun SimpleTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    searchBar: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp)
                .padding(bottom = if (searchBar != null) 12.dp else 4.dp)
        ) {
            // Title row
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button — only render if onBackClick is not null
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector      = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint             = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.padding(horizontal = 24.dp))
                }

                // Centred title takes up all the remaining space
                Box(
                    modifier         = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Trailing slot — same size as back button so title stays centred
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    // Empty spacer to balance the back button
                    Spacer(modifier = Modifier.padding(horizontal = 24.dp))
                }
            }

            // Optional search bar below the title row
            if (searchBar != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    searchBar()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SimpleTopBarPreview() {
    SimpleTopBar(title = "My Cart", onBackClick = {})
}

@Preview(showBackground = true)
@Composable
fun SimpleTopBarWithSearchPreview() {
    SimpleTopBar(
        title       = "Veterinary Care",
        onBackClick = {},
        searchBar   = {
            Text("[ search bar ]", color = androidx.compose.ui.graphics.Color.Gray)
        }
    )
}