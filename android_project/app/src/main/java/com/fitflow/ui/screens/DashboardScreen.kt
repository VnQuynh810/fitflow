package com.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.ui.theme.*

@Composable
fun DashboardScreen(onStartWorkout: () -> Unit) {
    // Mutable state để UI tự động cập nhật khi người dùng bấm nút
    var steps by remember { mutableIntStateOf(0) }
    var water by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HeaderSection() }
        
        item {
            // Hero CTA: Start Session
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentNeon),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartWorkout() },
            ) {
                Row(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("INITIALIZE", color = BackgroundDark, fontSize = 24.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                        Text("PROTOCOL", color = BackgroundDark, fontSize = 24.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("TODAY: PUSH UPS", color = BackgroundDark.copy(alpha=0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Box(
                        modifier = Modifier.size(56.dp).background(BackgroundDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Lucide.Activity, contentDescription = null, tint = AccentNeon, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        item { StreakSummarySection() }
        
        item {
            HealthMetricsSection(
                steps = steps,
                onAddSteps = { steps += 500 },
                water = water,
                onAddWater = { water += 250 }
            )
        }
        
        item {
            TodaySessionSection()
        }
        
        item { Spacer(modifier = Modifier.height(120.dp)) }
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
            Icon(Lucide.Bell, contentDescription = "Notify", tint = White40)
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
                Icon(Lucide.Trophy, contentDescription = null, tint = AccentNeon.copy(alpha = 0.5f))
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
                Icon(Lucide.Activity, contentDescription = null, tint = SecondaryBlue.copy(alpha = 0.5f))
                Column {
                    Text("WEIGHT LOSS", fontSize = 18.sp, color = SecondaryBlue, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                    Text("GIAI ĐOẠN 1", fontSize = 10.sp, color = White40, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@Composable
fun HealthMetricsSection(
    steps: Int, onAddSteps: () -> Unit,
    water: Int, onAddWater: () -> Unit
) {
    Text("HEALTH METRICS", fontSize = 11.sp, color = White40, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
    Spacer(modifier = Modifier.height(12.dp))
    
    MetricCard("STEPS", steps.toString(), 10000, "steps", AccentNeon, Lucide.Footprints, onAddSteps)
    Spacer(modifier = Modifier.height(12.dp))
    MetricCard("WATER", water.toString(), 2500, "ml", SecondaryBlue, Lucide.Droplets, onAddWater)
}

@Composable
fun MetricCard(label: String, value: String, goal: Int, unit: String, mainColor: androidx.compose.ui.graphics.Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val progress = (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp).border(1.dp, White05, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(modifier = Modifier.size(40.dp).background(mainColor.copy(alpha=0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = mainColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = White40, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("$value / $goal $unit", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(White05, RoundedCornerShape(50))) {
                    if (progress > 0f) {
                        Box(modifier = Modifier.fillMaxWidth(progress).height(4.dp).background(mainColor, RoundedCornerShape(50)))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            // Clickable Add Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, White10, RoundedCornerShape(12.dp))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Lucide.Plus, contentDescription = "Add", tint = TextDim)
            }
        }
    }
}

@Composable
fun TodaySessionSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SESSION", fontSize = 11.sp, color = White40, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text(
                "LỊCH TẬP THÁNG",
                color = AccentNeon,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.clickable {  }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Example Session Item (Push Ups)
        SessionItemCard(name = "PUSH UPS", details = "15 reps • 3 sets", iconLetter = "P")
        Spacer(modifier = Modifier.height(12.dp))
        // Example Session Item (Plank)
        SessionItemCard(name = "PLANK", details = "60s • 3 sets", iconLetter = "P")
    }
}

@Composable
fun SessionItemCard(name: String, details: String, iconLetter: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, White05, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(White05, RoundedCornerShape(12.dp))
                        .border(1.dp, White05, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconLetter, color = White20, fontSize = 18.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(name, color = TextDim, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(details, color = White30, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, White10, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Lucide.CheckCircle, contentDescription = null, tint = White10, modifier = Modifier.size(16.dp))
            }
        }
    }
}
