package com.niranjan.physiotimer.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseStep

@Composable
internal fun WellnessScreen(
    modifier: Modifier = Modifier,
    showOrganicBackground: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showOrganicBackground) {
            OrganicBackground()
            SoftTextureOverlay()
        }
        content()
    }
}

@Composable
internal fun OrganicBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 170.dp, y = (-110).dp)
                .clip(CircleShape)
                .background(WellnessColors.Sage300.copy(alpha = 0.16f))
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-120).dp, y = 530.dp)
                .clip(CircleShape)
                .background(WellnessColors.Lavender200.copy(alpha = 0.14f))
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 220.dp, y = 620.dp)
                .clip(CircleShape)
                .background(WellnessColors.Beige200.copy(alpha = 0.24f))
        )
    }
}

@Composable
private fun SoftTextureOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val diagonalSpacing = 52.dp.toPx()
        val dotSpacing = 30.dp.toPx()

        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = WellnessColors.Sage300.copy(alpha = 0.055f),
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = 1f
            )
            x += diagonalSpacing
        }

        var row = 0
        var y = 0f
        while (y < size.height) {
            var dx = if (row % 2 == 0) 0f else dotSpacing * 0.5f
            while (dx < size.width) {
                drawCircle(
                    color = WellnessColors.Ink700.copy(alpha = 0.04f),
                    radius = 1.05f,
                    center = Offset(dx, y)
                )
                dx += dotSpacing
            }
            row += 1
            y += dotSpacing
        }
    }
}

@Composable
internal fun WellnessCard(
    modifier: Modifier = Modifier,
    containerColor: Color = WellnessSurfaces.Card,
    shape: Shape = RoundedCornerShape(28.dp),
    textured: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Box {
            if (textured) {
                CardTextureOverlay(modifier = Modifier.fillMaxSize())
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun CardTextureOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val spacing = 18.dp.toPx()
        var y = spacing * 0.5f
        while (y < size.height) {
            drawLine(
                color = WellnessColors.Beige200.copy(alpha = 0.055f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.9f
            )
            y += spacing
        }
    }
}

@Composable
internal fun PrimaryWellnessButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(WellnessRadius.Pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun TonalWellnessButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(WellnessRadius.Pill),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(46.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun SoftIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(46.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun WellnessChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WellnessRadius.Pill),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

@Composable
internal fun GentleProgressArc(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    trackColor: Color = WellnessColors.Sage100,
    isRunning: Boolean = false,
    strokeWidth: Dp = 18.dp,
    startAngle: Float = 145f,
    sweepAngle: Float = 250f
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(durationMillis = 180, easing = LinearEasing),
        label = "progress"
    )
    val pulse = rememberInfiniteTransition(label = "pulse")
    val haloAlpha by pulse.animateFloat(
        initialValue = 0.08f,
        targetValue = if (isRunning) 0.18f else 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawCircle(
                color = activeColor.copy(alpha = if (isRunning) haloAlpha else 0.05f),
                radius = size.minDimension * 0.34f,
                center = center
            )

            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            drawArc(
                brush = Brush.linearGradient(listOf(activeColor, secondaryColor)),
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }
    }
}

@Composable
internal fun StepTimeline(
    steps: List<ExerciseStep>,
    activeIndex: Int,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val accent = composeColor(step.colorArgb)
            val isActive = index == activeIndex
            val isCompleted = index < activeIndex
            val targetWidth = if (isActive) 124.dp else 34.dp
            val width by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = tween(if (reducedMotion) 0 else 220),
                label = "timelineWidth"
            )
            val targetContainer = when {
                isActive -> accent.copy(alpha = 0.18f)
                isCompleted -> accent.copy(alpha = 0.14f)
                else -> MaterialTheme.colorScheme.surface
            }
            val animatedContainer by animateColorAsState(
                targetValue = targetContainer,
                animationSpec = tween(if (reducedMotion) 0 else 220),
                label = "timelineColor"
            )
            val container = if (reducedMotion) targetContainer else animatedContainer

            Surface(
                modifier = Modifier
                    .width(width)
                    .height(36.dp),
                shape = RoundedCornerShape(WellnessRadius.Pill),
                color = container,
                border = BorderStroke(
                    1.dp,
                    if (isActive) accent.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
                ),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted || isActive) accent else accent.copy(alpha = 0.35f))
                    )
                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${step.name} ${step.durationSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineCard(
    exercise: Exercise,
    supportingText: String,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isCompleted: Boolean = false,
    isExpanded: Boolean = false,
    onToggleExpanded: () -> Unit = {},
    startLabel: String = "Start"
) {
    val accent = exerciseAccent(exercise)
    val cardContainerColor = if (isCompleted) {
        WellnessColors.Sage75
    } else {
        WellnessSurfaces.Card
    }

    WellnessCard(
        modifier = Modifier.animateContentSize(),
        shape = RoundedCornerShape(30.dp),
        containerColor = cardContainerColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(WellnessRadius.Pill))
                .background(accent.copy(alpha = if (isCompleted) 0.72f else 0.45f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onToggleExpanded)
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.ifBlank { "Untitled routine" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse routine" else "Expand routine",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            QuickStartButton(
                text = startLabel,
                onClick = onStart
            )
        }

        if (isExpanded) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoutineStatCell(
                    label = "Steps",
                    value = exercise.steps.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                RoutineStatCell(
                    label = "Reps",
                    value = exercise.reps.toString(),
                    modifier = Modifier.weight(1f)
                )
                RoutineStatCell(
                    label = "Time",
                    value = formatClock(exercise.totalSeconds),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconButton(
                    iconRes = AppIcons.settings,
                    contentDescription = "Edit routine",
                    onClick = onEdit
                )
                if (onDelete != null && exercise.id != 0L) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStartButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(WellnessRadius.Pill),
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun RoutineStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = WellnessSurfaces.CardMuted,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun SettingsSection(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    WellnessCard(modifier = modifier, containerColor = WellnessSurfaces.CardMuted) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
internal fun StepperRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Stepper(
            value = value,
            onValueChange = { onValueChange(it.coerceAtLeast(min)) }
        )
    }
}

@Composable
internal fun CompactTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WellnessColors.Sage50)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun CompactNumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    suffix: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WellnessColors.Sage50)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
            }
            BasicTextField(
                value = if (value == 0) "" else value.toString(),
                onValueChange = { text -> onValueChange(text.filter { it.isDigit() }.toIntOrNull() ?: 0) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            if (suffix.isNotBlank()) {
                Text(suffix, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun CompactNumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    suffix: String,
    @DrawableRes iconRes: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WellnessColors.Sage50)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            BasicTextField(
                value = if (value == 0) "" else value.toString(),
                onValueChange = { text -> onValueChange(text.filter { it.isDigit() }.toIntOrNull() ?: 0) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            if (suffix.isNotBlank()) {
                Text(suffix, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun SmallSwitch(checked: Boolean, color: Color, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = color,
            uncheckedThumbColor = WellnessColors.Ink300,
            uncheckedTrackColor = WellnessColors.Beige200
        )
    )
}

@Composable
internal fun Stepper(value: Int, onValueChange: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = WellnessColors.Sage50,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .width(134.dp)
                .height(48.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SoftIconButton(
                icon = Icons.Default.Remove,
                contentDescription = "Decrease",
                modifier = Modifier.size(36.dp),
                onClick = { onValueChange(value - 1) }
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            SoftIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Increase",
                modifier = Modifier.size(36.dp),
                onClick = { onValueChange(value + 1) }
            )
        }
    }
}

@Composable
internal fun ErrorPanel(errors: List<String>) {
    WellnessCard(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = "Needs a quick fix",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        errors.forEach { message ->
            Text(
                text = "- $message",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
internal fun TealIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    SoftIconButton(icon = icon, contentDescription = contentDescription, onClick = onClick)
}

@Composable
internal fun TealIconButton(@DrawableRes iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    SoftIconButton(iconRes = iconRes, contentDescription = contentDescription, onClick = onClick)
}

@Composable
internal fun SoftControlButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TonalWellnessButton(text = text, icon = icon, modifier = modifier, enabled = enabled, onClick = onClick)
}

@Composable
internal fun SoftControlButton(
    text: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(WellnessRadius.Pill),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun RowScope.BottomItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    WellnessBottomItem(selected = selected, label = label, onClick = onClick) {
        Icon(icon, contentDescription = label)
    }
}

@Composable
internal fun RowScope.BottomItem(
    selected: Boolean,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    WellnessBottomItem(selected = selected, label = label, onClick = onClick) {
        Icon(painterResource(iconRes), contentDescription = label)
    }
}

@Composable
private fun RowScope.WellnessBottomItem(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
