package com.example.android_fefu_homeworks_splitmate.data

data class Calculation(
    val id: String,
    val total: Double,
    val people: Int,
    val tipPercent: Int = 10 // по умолчанию 10%
) {
    val tipAmount: Double
        get() = total * tipPercent / 100.0

    val totalWithTip: Double
        get() = total + tipAmount

    val perPerson: Double
        get() = totalWithTip / people
}
