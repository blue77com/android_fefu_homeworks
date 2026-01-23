package com.example.android_fefu_homeworks.model

data class Holiday(
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String,
    val global: Boolean,
    val counties: List<String>?,
    val launchYear: Int?,
    val types: List<String>
) {
    val id: String
        get() {
            val countiesStr = counties?.joinToString(",") ?: "global"
            return "${countryCode}_${date}_${name}_${localName}_${countiesStr}"
        }
    
    val translatedTypes: List<String>
        get() = types.map { it.translateHolidayType() }
}

private fun String.translateHolidayType(): String {
    return when (this.lowercase()) {
        "public" -> "Государственный праздник"
        "bank" -> "Банковский выходной"
        "school" -> "Школьные каникулы"
        "authorities" -> "Выходной в госучреждениях"
        "optional" -> "Необязательный праздник"
        "observance" -> "Памятная дата"
        else -> this
    }
}
