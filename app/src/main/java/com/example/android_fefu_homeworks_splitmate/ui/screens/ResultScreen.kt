package com.example.android_fefu_homeworks_splitmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android_fefu_homeworks_splitmate.ui.navigation.Screen
import com.example.android_fefu_homeworks_splitmate.viewmodel.SplitViewModel

@Composable
fun ResultScreen(
    calcId: String,
    viewModel: SplitViewModel,
    navController: NavController
) {
    val calculation = viewModel.calculationById(calcId)
    val isFromHistory = viewModel.isResultFromHistory(calcId)

    if (calculation == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Calculation not found",
                fontSize = 22.sp,
                color = Color(0xFFdddddd)
            )
        }
        return
    }

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
                "Calculation Result",
                fontSize = 28.sp,
                color = Color(0xFFdddddd),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222), RoundedCornerShape(15.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResultRow("Bill Total:", "%.2f".format(calculation.total))
                    Spacer(Modifier.height(12.dp))
                    ResultRow("Tip amount:", "%.2f".format(calculation.tipAmount))
                    Spacer(Modifier.height(12.dp))
                    ResultRow("Total with tip:", "%.2f".format(calculation.totalWithTip))
                    Spacer(Modifier.height(12.dp))
                    ResultRow("Per person:", "%.2f".format(calculation.perPerson), isHighlighted = true)
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            if (!isFromHistory) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            viewModel.setEditingCalculation(calcId)
                            navController.navigate(Screen.Input.route)
                        }
                        .background(Color(0xFF222222), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Back to edit",
                        fontSize = 22.sp,
                        color = Color(0xFFdddddd)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { navController.navigate(Screen.History.route) }
                    .background(Color(0xFF222222), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "History",
                    fontSize = 22.sp,
                    color = Color(0xFFdddddd)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        viewModel.reset()
                        navController.navigate(Screen.Input.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                    .background(Color(0xFF222222), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "New calculation",
                    fontSize = 22.sp,
                    color = Color(0xFFdddddd)
                )
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = if (isHighlighted) 22.sp else 18.sp,
            color = Color(0xFFdddddd),
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            fontSize = if (isHighlighted) 24.sp else 18.sp,
            color = Color(0xFFdddddd),
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}
