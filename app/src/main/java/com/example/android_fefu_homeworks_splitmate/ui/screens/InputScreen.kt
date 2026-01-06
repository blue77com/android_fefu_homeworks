package com.example.android_fefu_homeworks_splitmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_fefu_homeworks_splitmate.viewmodel.SplitViewModel

@Composable
fun InputScreen(
    viewModel: SplitViewModel,
    onCalculate: (String) -> Unit
) {
    val state = viewModel.uiState
    
    var totalText by remember(state.lastTotalText) { mutableStateOf(state.lastTotalText) }
    var peopleText by remember(state.lastPeopleText) { mutableStateOf(state.lastPeopleText) }

    val totalValue = totalText.toDoubleOrNull()
    val peopleValue = peopleText.toIntOrNull()
    
    val isValid = viewModel.canCreateCalculation(totalText, peopleText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Enter Bill Details",
            fontSize = 24.sp,
            color = Color(0xFFdddddd),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = totalText,
            onValueChange = { newValue ->
                if (viewModel.isValidDecimalInput(newValue)) {
                    totalText = newValue
                }
            },
            label = { Text("Total", color = Color(0xFFdddddd)) },
            placeholder = { Text("100.50", color = Color(0x88dddddd)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFFdddddd),
                unfocusedTextColor = Color(0xFFdddddd),
                focusedContainerColor = Color(0xFF222222),
                unfocusedContainerColor = Color(0xFF222222),
                focusedIndicatorColor = Color(0xFFdddddd),
                unfocusedIndicatorColor = Color(0xFF444444)
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = peopleText,
            onValueChange = { newValue ->
                if (viewModel.isValidNumberInput(newValue)) {
                    peopleText = newValue
                }
            },
            label = { Text("People", color = Color(0xFFdddddd)) },
            placeholder = { Text("3", color = Color(0x88dddddd)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFFdddddd),
                unfocusedTextColor = Color(0xFFdddddd),
                focusedContainerColor = Color(0xFF222222),
                unfocusedContainerColor = Color(0xFF222222),
                focusedIndicatorColor = Color(0xFFdddddd),
                unfocusedIndicatorColor = Color(0xFF444444)
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                totalValue?.let { total ->
                    peopleValue?.let { people ->
                        viewModel.createCalculation(total, people)
                        viewModel.currentCalculation?.id?.let(onCalculate)
                    }
                }
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF222222),
                contentColor = Color(0xFFdddddd),
                disabledContainerColor = Color(0x55222222),
                disabledContentColor = Color(0x55dddddd)
            )
        ) {
            Text("Calculate", fontSize = 20.sp)
        }
    }
}
