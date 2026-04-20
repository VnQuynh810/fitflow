package com.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.ui.theme.*

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        HeaderSection()
        Spacer(modifier = Modifier.height(32.dp))
        StreakSummarySection()
        Spacer(modifier = Modifier.height(32.dp))
        HealthMetricsSection()
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "STATUS REPORT",
                color = White40,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Row {
                Text("RISE & ", color = TextDim, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("GRIND", color = AccentNeon, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
        }
        IconButton(
            onClick = {},
            modifier = Modifier
                .background(White05, RoundedCornerShape(50))
                .border(1.dp, White05, RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notify", tint = White40)
        }
    }
}

@Composable
fun StreakSummarySection() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.weight(1f).height(160.dp).border(1.dp, White05, RoundedCornerShape(32.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.Star, contentDescription = null, tint = AccentNeon.copy(alpha = 0.5f))
                Column {
                    Text("0", fontSize = 48.sp, color = AccentNeon, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                    Text("CHUỖI STREAK", fontSize = 10.sp, color = White40, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.weight(1f).height(160.dp).border(1.dp, White05, RoundedCornerShape(32.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = White40.copy(alpha = 0.5f))
                Column {
                    Text("WEIGHT LOSS", fontSize = 18.sp, color = TextDim, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                    Text("GIAI ĐOẠN 1", fontSize = 10.sp, color = White40, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@Composable
fun HealthMetricsSection() {
    Text("HEALTH METRICS", fontSize = 11.sp, color = White40, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
    Spacer(modifier = Modifier.height(12.dp))
    
    MetricCard("STEPS", "0", "10000 steps")
    Spacer(modifier = Modifier.height(12.dp))
    MetricCard("WATER", "0", "2500 ml")
}

@Composable
fun MetricCard(label: String, value: String, goal: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp).border(1.dp, White05, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(AccentNeon.copy(alpha=0.1f), RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = White40, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("$value / $goal", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(White05, RoundedCornerShape(50)))
            }
        }
    }
}
