package com.example.fitnesapp.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitnesapp.R
import com.example.fitnesapp.presentation.theme.AccentOrange
import com.example.fitnesapp.presentation.theme.LocalFitnesThemeExtras
import com.example.fitnesapp.presentation.theme.SurfaceDark

@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine
    )
}

@Composable
fun EmptyState(title: String, description: String, imageRes: Int? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageRes?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp))
            )
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun ChipRow(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { item ->
                    FilterChip(selected = selected == item, onClick = { onSelected(item) }, label = { Text(item) })
                }
            }
        }
    }
}

@Composable
fun StatTile(title: String, value: String) {
    val extras = LocalFitnesThemeExtras.current
    Column(
        modifier = Modifier
            .background(if (extras.isSport) MaterialTheme.colorScheme.surfaceVariant else SurfaceDark, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .width(120.dp)
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SimpleLineChart(values: List<Float>, labels: List<String>, modifier: Modifier = Modifier) {
    if (values.isEmpty()) {
        EmptyState("Нет данных", "Пока недостаточно записей для построения графика")
        return
    }
    val max = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val chartBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            val stepX = if (values.size == 1) size.width else size.width / (values.size - 1)
            val points = values.mapIndexed { index, value ->
                Offset(index * stepX, size.height - (value / max) * (size.height - 16.dp.toPx()))
            }
            for (i in 0 until points.lastIndex) {
                drawLine(AccentOrange, points[i], points[i + 1], strokeWidth = 8f, cap = StrokeCap.Round)
            }
            points.forEach { point ->
                drawCircle(AccentOrange, radius = 8f, center = point)
            }
            drawRect(chartBorderColor, style = Stroke(width = 2f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.takeLast(4).forEach { label ->
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ImageBannerCard(
    imageRes: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val extras = LocalFitnesThemeExtras.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(170.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(extras.glowColor.copy(alpha = if (extras.isSport) 0.18f else 0.08f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    selected: Boolean,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp))
            )
            Text(title, fontWeight = FontWeight.Bold)
            Text(if (selected) "Активно" else "Нажмите для выбора", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
