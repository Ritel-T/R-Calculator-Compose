package com.ritel.calculator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Simple: Screen(
        route = "simple",
        title = "Simple",
        icon = Icons.Filled.Calculate
    )
    data object Scientific: Screen(
        route = "scientific",
        title = "Scientific",
        icon = Icons.Filled.Science
    )
}