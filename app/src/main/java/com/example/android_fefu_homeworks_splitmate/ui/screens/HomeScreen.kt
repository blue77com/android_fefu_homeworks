package com.example.android_fefu_homeworks_splitmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun HomeScreen(onStartClick: () -> Unit, onHistoryClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "SplitMate",
                fontSize = 36.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Split the bill easily",
                fontSize = 20.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color(0xFFdddddd)
                ),
                modifier = Modifier
                    .height(56.dp)
                    .width(200.dp)
            ) {
                Text("Start", fontSize = 22.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onHistoryClick,
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color(0xFFdddddd)
                ),
                modifier = Modifier
                    .height(56.dp)
                    .width(200.dp)
            ) {
                Text("History", fontSize = 22.sp)
            }
        }
    }
}
