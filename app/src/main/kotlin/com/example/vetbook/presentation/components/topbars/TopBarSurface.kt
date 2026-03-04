package com.example.vetbook.presentation.components.topbars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Common shell for the Type-A (main-tab) yellow top bar.
 * Used by StoreHeader and HomeTopBar.
 */
@Composable
fun TopBarSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.primary, // Brand gold
        shadowElevation = 4.dp,
        shape           = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 12.dp),
            content = content
        )
    }
}
