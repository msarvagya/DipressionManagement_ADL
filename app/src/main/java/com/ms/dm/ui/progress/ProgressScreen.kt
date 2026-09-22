package com.ms.dm.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ms.dm.data.DailyMood
import com.ms.dm.data.getInitial7DaysMood
import com.ms.dm.ui.theme.DMTheme
import java.util.Locale

@Composable
fun ProgressScreen(
    dailyMoods: List<DailyMood>,
    innerPadding: PaddingValues
) {
    val scrollState = rememberScrollState()

    // Analytics calculations
    val loggedDays = dailyMoods.count { it.mood != null }
    val averageScore = if (loggedDays > 0) {
        dailyMoods.mapNotNull { it.mood?.score }.average()
    } else {
        0.0
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Title
        Column {
            Text(
                text = "Weekly Progress",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your energy & mood patterns over time.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }

        // Summary Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Average Mood Score: ${String.format(Locale.US, "%.1f", averageScore)} out of 4"
                    },
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Avg Mood",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = onSurfaceColor.copy(alpha = 0.6f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (loggedDays > 0) String.format(Locale.US, "%.1f / 4.0", averageScore) else "N/A",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    )
                    Text(
                        text = getAverageMoodLabel(averageScore),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Weekly Consistency: $loggedDays out of 7 days logged"
                    },
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logged",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = onSurfaceColor.copy(alpha = 0.6f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$loggedDays / 7 Days",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    )
                    Text(
                        text = "${((loggedDays * 100) / 7)}% consistency",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = onSurfaceColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        // 7-Day Mood Graph Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "7-Day Mood Chart showing daily energy levels over the past week."
                },
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Past 7 Days Graph",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = onSurfaceColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = onSurfaceColor.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom 7-Day Chart
                MoodBarChart(
                    dailyMoods = dailyMoods,
                    primaryColor = primaryColor,
                    surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Breakdown History List Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Daily Mood Log",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                )

                dailyMoods.reversed().forEach { item ->
                    DailyLogRow(item = item)
                }
            }
        }
    }
}

@Composable
fun MoodBarChart(
    dailyMoods: List<DailyMood>,
    primaryColor: Color,
    surfaceVariantColor: Color
) {
    val emojis = listOf("☀️", "⛅", "🌥️", "☁️")

    Row(modifier = Modifier.fillMaxWidth()) {
        // Y-Axis Emoji Labels
        Column(
            modifier = Modifier
                .height(180.dp)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            emojis.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 14.sp
                )
            }
        }

        // Canvas Bar Graph
        Box(
            modifier = Modifier
                .weight(1f)
                .height(210.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = dailyMoods.size
                val barWidth = (canvasWidth / barCount) * 0.45f
                val spacing = canvasWidth / barCount

                // Draw background horizontal grid lines
                for (i in 0..3) {
                    val y = canvasHeight * (i / 3f)
                    drawLine(
                        color = surfaceVariantColor.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 2f
                    )
                }

                // Draw mood bars and connection line
                val points = mutableListOf<Offset>()

                dailyMoods.forEachIndexed { index, item ->
                    val score = item.mood?.score ?: 0
                    val x = index * spacing + spacing / 2f
                    val normalizedHeight = if (score > 0) (score / 4f) else 0.05f
                    val barHeight = canvasHeight * normalizedHeight
                    val topY = canvasHeight - barHeight

                    val leftX = x - barWidth / 2f

                    // Gradient bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.3f))
                        ),
                        topLeft = Offset(leftX, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    if (score > 0) {
                        points.add(Offset(x, topY))
                    }
                }

                // Draw trend connection line across bars
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.8f),
                        style = Stroke(width = 4f)
                    )
                }

                // Draw highlight points on top of bars
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 7f,
                        center = point
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 5f,
                        center = point
                    )
                }
            }

            // X-Axis Day Labels below Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dailyMoods.forEach { item ->
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (item.dayLabel == "Sun") FontWeight.Bold else FontWeight.Normal,
                            color = if (item.dayLabel == "Sun") primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DailyLogRow(item: DailyMood) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.dayLabel}: ${item.mood?.label ?: "Not logged"}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.mood?.emoji ?: "❓",
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.dayLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = item.mood?.label ?: "No entry",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (item.mood != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else Color.Gray.copy(alpha = 0.1f)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (item.mood != null) "${item.mood.score} / 4" else "Pending",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (item.mood != null) MaterialTheme.colorScheme.primary else Color.Gray
                )
            )
        }
    }
}

private fun getAverageMoodLabel(score: Double): String {
    return when {
        score >= 3.5 -> "Great Energy ☀️"
        score >= 2.5 -> "Okay / Moderate ⛅"
        score >= 1.5 -> "Low Energy 🌥️"
        score > 0.0 -> "Very Low Energy ☁️"
        else -> "No Data"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
fun ProgressScreenPreview() {
    DMTheme {
        ProgressScreen(
            dailyMoods = getInitial7DaysMood(),
            innerPadding = PaddingValues(0.dp)
        )
    }
}
