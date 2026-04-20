package com.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.ui.theme.*

@Composable
fun LibraryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DATABASE", color = TextDim, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Màn hình danh sách Custom Exercises cho phép thêm/sửa/xoá.", 
            color = White40, 
            fontSize = 14.sp
        )
    }
}
