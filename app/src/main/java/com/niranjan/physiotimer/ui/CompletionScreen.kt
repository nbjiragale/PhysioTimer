package com.niranjan.physiotimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.niranjan.physiotimer.data.Exercise

@Composable
internal fun CompletionScreen(
    exercise: Exercise,
    onDone: () -> Unit,
    onRepeat: () -> Unit
) {
    WellnessScreen(showOrganicBackground = false) {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = WellnessSpacing.Xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(72.dp))
                SuccessCheck()
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "Well done",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "You completed your recovery flow.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Replay,
                        label = "Reps completed",
                        value = exercise.reps.toString(),
                        tint = WellnessColors.Lavender600,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        iconRes = AppIcons.timer,
                        label = "Total time",
                        value = formatClock(exercise.totalSeconds),
                        tint = WellnessColors.Sage600,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.weight(1f))

                PrimaryWellnessButton(
                    text = "Done",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDone
                )
                Spacer(Modifier.height(12.dp))
                TonalWellnessButton(
                    text = "Do again",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRepeat
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
internal fun SuccessCheck() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(156.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = WellnessColors.Sage200.copy(alpha = 0.42f), radius = size.minDimension * 0.48f, center = center)
            drawCircle(color = WellnessColors.Lavender200.copy(alpha = 0.38f), radius = size.minDimension * 0.34f, center = center)
        }
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(AppIcons.successCheck),
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(58.dp)
            )
        }
    }
}

@Composable
internal fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    WellnessCard(modifier = modifier, containerColor = WellnessSurfaces.CardMuted, textured = false) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun StatCard(
    @androidx.annotation.DrawableRes iconRes: Int,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    WellnessCard(modifier = modifier, containerColor = WellnessSurfaces.CardMuted, textured = false) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
