package io.github.vnquynh810.fitflow.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.vnquynh810.fitflow.core.designsystem.theme.CardBackground
import io.github.vnquynh810.fitflow.core.designsystem.theme.CardNested
import io.github.vnquynh810.fitflow.core.designsystem.theme.OrangeGlow
import io.github.vnquynh810.fitflow.core.designsystem.theme.OrangePrimary
import io.github.vnquynh810.fitflow.core.model.Exercise
import java.util.Locale

@Composable
internal fun ReplaceExerciseDialog(
    exerciseName: String,
    category: String,
    allDatabaseExercises: List<Exercise>,
    gifUrlsByFileName: Map<String, String>,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit,
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, allDatabaseExercises) {
        allDatabaseExercises.filter { dbEx ->
            dbEx.name.contains(searchQuery, ignoreCase = true) ||
                dbEx.exerciseType.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "REPLACE EXERCISE",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 20.sp,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = "Replacing: $exerciseName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        cursorColor = OrangePrimary,
                    ),
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filtered.size) { index ->
                        val dbEx = filtered[index]
                        val matchesCategory = dbEx.exerciseType.equals(category, ignoreCase = true)
                        val itemAccentColor = if (matchesCategory) OrangePrimary else Color.Gray
                        val gifName = dbEx.localGifs.firstOrNull()?.trim().orEmpty()
                        val dialogGifUrl = gifUrlsByFileName[gifName]

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (matchesCategory) OrangeGlow else CardNested,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(dbEx) },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(itemAccentColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (!dialogGifUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(dialogGifUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = dbEx.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp)),
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = itemAccentColor,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dbEx.exerciseType.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = itemAccentColor,
                                        fontSize = 10.sp,
                                    )
                                    Text(
                                        text = dbEx.name.uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                    )
                                    val difficultyLabel = dbEx.difficulty.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    }
                                    val equipmentLabel = dbEx.equipment.joinToString(", ").replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    }
                                    Text(
                                        text = "$difficultyLabel · $equipmentLabel",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
