package com.fitflow.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WorkoutSessionScreen(onComplete: () -> Unit, onExit: () -> Unit) {
    var currentExIndex by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var isActive by remember { mutableStateOf(true) }

    val exercises = listOf("PUSHUPS", "SQUATS", "PLANK", "BURPEES")
    val currentExName = exercises[currentExIndex]

    // Timer Logic
    LaunchedEffect(isActive, timeLeft) {
        if (isActive && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        } else if (timeLeft == 0) {
            isActive = false
        }
    }

    // Progress Animation
    val progress by animateFloatAsState(
        targetValue = timeLeft / 30f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "TimerProgress"
    )

    // Pulse Animation for "Animation Area"
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SESSION ACTIVE", color = AccentNeon, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text(currentExName, color = TextDim, fontSize = 24.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
            IconButton(
                onClick = onExit,
                modifier = Modifier.background(White05, CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit", tint = White40)
            }
        }

        // Animation / Timer Central Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Pulse Glow
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(AccentNeon.copy(alpha = 0.15f * pulseScale), CircleShape)
                        .blur(40.dp)
                )
            }

            // Progress Ring
            Canvas(modifier = Modifier.size(240.dp)) {
                drawArc(
                    color = White05,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = AccentNeon,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Time text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$timeLeft",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    "SECONDS",
                    color = AccentNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            }
        }

        // Reps/Sets Info
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "3 SETS × 15 REPS",
                color = SecondaryBlue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
            Text(
                "INTENSITY VECTOR ALPHA",
                color = White40,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        }

        // Footer Controls
        Surface(
            color = CardDark.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("NEXT: ${if (currentExIndex < exercises.size -1) exercises[currentExIndex+1] else "FINISH"}", color = White40, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${currentExIndex + 1} / ${exercises.size}", color = SecondaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { isActive = !isActive },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) White05 else SecondaryBlue
                        ),
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            if (isActive) "PAUSE" else "RESUME",
                            color = if (isActive) Color.White else BackgroundDark,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (currentExIndex < exercises.size - 1) {
                                currentExIndex += 1
                                timeLeft = 30
                                isActive = true
                            } else {
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeon),
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            if (currentExIndex == exercises.size - 1) "FINISH" else "NEXT",
                            color = BackgroundDark,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}
