package com.example.android_fefu_homeworks_splitmate.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android_fefu_homeworks_splitmate.data.Calculation
import java.util.*

data class SplitUiState(
    val lastTotalText: String = "",
    val lastPeopleText: String = "",
    val editingCalculationId: String? = null,
    val resultsFromHistory: Set<String> = emptySet()
)

class SplitViewModel : ViewModel() {

    private var calculations by mutableStateOf<List<Calculation>>(emptyList())

    var uiState by mutableStateOf(SplitUiState())
        private set

    val currentCalculation: Calculation?
        get() = calculations.firstOrNull()

    val lastCalculations: List<Calculation>
        get() = calculations.take(5)

    fun calculationById(id: String): Calculation? =
        calculations.find { it.id == id }

    fun isValidNumberInput(text: String): Boolean {
        if (text.isEmpty()) return true
        return text.matches(Regex("^\\d+$")) && !text.matches(Regex("^0+\\d+$"))
    }

    fun isValidDecimalInput(text: String): Boolean {
        if (text.isEmpty()) return true
        if (!text.matches(Regex("^\\d*\\.?\\d*$"))) return false
        if (text.contains('.')) {
            val integerPart = text.split('.').first()
            return integerPart.isEmpty() || integerPart == "0" || !integerPart.startsWith("0")
        }
        return !text.matches(Regex("^0+\\d+$"))
    }

    fun canCreateCalculation(totalText: String, peopleText: String): Boolean {
        val total = totalText.toDoubleOrNull()
        val people = peopleText.toIntOrNull()
        return total != null && total > 0 && people != null && people > 0
    }

    fun createCalculation(total: Double, people: Int, tipPercent: Int = 10) {
        val calculation = uiState.editingCalculationId?.let { editingId ->
            Calculation(editingId, total, people, tipPercent)
        } ?: run {
            Calculation(UUID.randomUUID().toString(), total, people, tipPercent)
        }

        calculations = if (uiState.editingCalculationId != null) {
            listOf(calculation) + calculations.filterNot { it.id == calculation.id }
        } else {
            listOf(calculation) + calculations
        }

        uiState = uiState.copy(
            editingCalculationId = null,
            lastTotalText = total.toString(),
            lastPeopleText = people.toString()
        )
    }

    fun setEditingCalculation(calcId: String?) {
        uiState = uiState.copy(editingCalculationId = calcId)
    }

    fun setResultFromHistory(calcId: String, fromHistory: Boolean) {
        uiState = if (fromHistory) {
            uiState.copy(resultsFromHistory = uiState.resultsFromHistory + calcId)
        } else {
            uiState.copy(resultsFromHistory = uiState.resultsFromHistory - calcId)
        }
    }

    fun isResultFromHistory(calcId: String): Boolean {
        return calcId in uiState.resultsFromHistory
    }

    fun reset() {
        uiState = uiState.copy(
            editingCalculationId = null,
            lastTotalText = "",
            lastPeopleText = ""
        )
    }
}
