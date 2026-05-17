package com.niranjan.physiotimer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.niranjan.physiotimer.ui.WellnessLightColorScheme
import com.niranjan.physiotimer.ui.WellnessShapes
import com.niranjan.physiotimer.ui.WellnessTypography

@Composable
fun PhysioTimerTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WellnessLightColorScheme,
        typography = WellnessTypography,
        shapes = WellnessShapes,
        content = content
    )
}
