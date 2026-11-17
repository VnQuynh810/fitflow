package io.github.vnquynh810.fitflow.feature.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun UnitToggle(
    left: String,
    right: String,
    isLeftSelected: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                RoundedCornerShape(14.dp),
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UnitPill(
            text = left,
            selected = isLeftSelected,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(true) },
        )
        UnitPill(
            text = right,
            selected = !isLeftSelected,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(false) },
        )
    }
}

@Composable
fun UnitPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            },
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RulerPicker(
    selectedValue: Int,
    values: List<Int>,
    majorTickEvery: Int,
    minorTickStep: Int,
    onSelect: (Int) -> Unit,
    majorTickColor: Color = MaterialTheme.colorScheme.primary,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tickSlotWidth = 16.dp
        val tickSpacing = 4.dp
        val horizontalPadding = (maxWidth / 2) - (tickSlotWidth / 2)
        val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)

        LaunchedEffect(selectedIndex) {
            if (!listState.isScrollInProgress && selectedIndex != listState.firstVisibleItemIndex) {
                listState.scrollToItem(selectedIndex)
            }
        }

        LaunchedEffect(listState, values) {
            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) {
                    null
                } else {
                    val viewportCenter =
                        (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                    visibleItems.minByOrNull { item ->
                        abs((item.offset + item.size / 2) - viewportCenter)
                    }?.index
                }
            }
                .distinctUntilChanged()
                .collect { centeredIndex ->
                    if (centeredIndex == null || centeredIndex !in values.indices) return@collect
                    val valueAtCenter = values[centeredIndex]
                    if (valueAtCenter != selectedValue) {
                        onSelect(valueAtCenter)
                    }
                }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                state = listState,
                flingBehavior = snapFlingBehavior,
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(tickSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(values) { index, value ->
                    val isSelected = value == selectedValue
                    RulerTick(
                        value = value,
                        isMajor = value % majorTickEvery == 0,
                        isMinor = value % minorTickStep == 0,
                        isSelected = isSelected,
                        majorTickColor = majorTickColor,
                        onClick = {
                            scope.launch { listState.animateScrollToItem(index) }
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(majorTickColor),
            )
        }
    }
}

@Composable
fun RulerTick(
    value: Int,
    isMajor: Boolean,
    isMinor: Boolean,
    isSelected: Boolean,
    majorTickColor: Color,
    onClick: () -> Unit,
) {
    val tickHeight: Dp = if (isMajor) 40.dp else if (isMinor) 24.dp else 14.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(16.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .width(if (isSelected) 3.dp else 2.dp)
                .height(tickHeight)
                .background(
                    if (isSelected) {
                        majorTickColor
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                    },
                    RoundedCornerShape(2.dp),
                ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isMajor) value.toString() else "",
            fontSize = 10.sp,
            color = if (isSelected) {
                majorTickColor
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            },
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun YearWheelPicker(
    selectedYear: Int,
    years: List<Int>,
    onYearChange: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val clampedSelectedYear = selectedYear.coerceIn(years.first(), years.last())
    val selectedIndex = years.indexOf(clampedSelectedYear).coerceAtLeast(0)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(listState, years) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                null
            } else {
                val viewportCenter =
                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }?.index
            }
        }
            .distinctUntilChanged()
            .collect { centeredIndex ->
                if (centeredIndex == null || centeredIndex !in years.indices) return@collect
                val yearAtCenter = years[centeredIndex]
                if (yearAtCenter != selectedYear) {
                    onYearChange(yearAtCenter)
                }
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = 56.dp),
        ) {
            items(years) { year ->
                val isSelected = year == selectedYear
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onYearChange(year)
                            val index = years.indexOf(year)
                            if (index >= 0) {
                                scope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = year.toString(),
                        fontSize = if (isSelected) 32.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                        },
                        fontStyle = FontStyle.Normal,
                    )
                }
            }
        }
    }
}
