package com.fitflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.ui.theme.*

@Composable
fun BottomNavbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark.copy(alpha = 0.9f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("Home", Icons.Default.Home, true)
        NavItem("Plan", Icons.Default.DateRange, false)
        
        // FAB 
        Box(
            modifier = Modifier
                .offset(y = (-24).dp)
                .size(64.dp)
                .background(AccentNeon, CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(32.dp))
        }

        NavItem("Library", Icons.Default.List, false)
        NavItem("Me", Icons.Default.Person, false)
    }
}

@Composable
fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    val color = if (selected) AccentNeon else White40
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
    }
}
