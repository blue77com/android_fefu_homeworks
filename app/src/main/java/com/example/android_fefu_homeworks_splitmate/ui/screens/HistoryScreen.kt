package com.example.android_fefu_homeworks_splitmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_fefu_homeworks_splitmate.viewmodel.SplitViewModel

@Composable
fun HistoryScreen(viewModel: SplitViewModel, onItemClick: (String) -> Unit) {
    val history = viewModel.lastCalculations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "Calculation History",
            fontSize = 28.sp,
            color = Color(0xFFdddddd),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Last 5 calculations",
            fontSize = 18.sp,
            color = Color(0xFFdddddd),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No calculations yet",
                        fontSize = 22.sp,
                        color = Color(0xFFdddddd),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Start a new calculation to see it here",
                        fontSize = 18.sp,
                        color = Color(0xFFdddddd),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(history) { calc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(calc.id) }
                            .background(Color(0xFF222222), RoundedCornerShape(15.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Total: ${"%.2f".format(calc.total)}",
                                    fontSize = 20.sp,
                                    color = Color(0xFFdddddd),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "People: ${calc.people}",
                                    fontSize = 18.sp,
                                    color = Color(0xFFdddddd)
                                )
                            }
                            Text(
                                "%.2f".format(calc.perPerson),
                                fontSize = 24.sp,
                                color = Color(0xFFdddddd),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
