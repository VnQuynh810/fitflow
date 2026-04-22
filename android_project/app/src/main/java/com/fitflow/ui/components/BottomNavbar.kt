package com.fitflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.ui.theme.*

@Composable
fun BottomNavbar(currentRoute: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark.copy(alpha = 0.95f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("Home", Icons.Outlined.Home, currentRoute == "dashboard") { onNavigate("dashboard") }
        NavItem("Plan", Icons.Outlined.DateRange, currentRoute == "planner") { onNavigate("planner") }
        
        // FAB 
        Box(
            modifier = Modifier
                .offset(y = (-24).dp)
                .size(64.dp)
                .background(AccentNeon, CircleShape)
                .clip(CircleShape)
                .clickable { onNavigate("workout_setup") }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(32.dp))
        }

        NavItem("Library", Icons.Outlined.List, currentRoute == "library") { onNavigate("library") }
        NavItem("Me", Icons.Outlined.Person, currentRoute == "profile") { onNavigate("profile") }
    }
}

@Composable
fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) AccentNeon else White40
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
    }
}
