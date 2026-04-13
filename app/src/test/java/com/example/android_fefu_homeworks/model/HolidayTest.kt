package com.example.android_fefu_homeworks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HolidayTest {

    @Test
    fun translatedTypes_translatesKnownTypes_caseInsensitive() {
        val holiday = Holiday(
            date = "2026-01-01",
            localName = "x",
            name = "x",
            countryCode = "RU",
            global = true,
            counties = null,
            types = listOf("Public", "bank", "School", "Authorities", "Optional", "Observance"),
        )

        assertEquals(
            listOf(
                "Государственный праздник",
                "Банковский выходной",
                "Школьные каникулы",
                "Выходной в госучреждениях",
                "Необязательный праздник",
                "Памятная дата",
            ),
            holiday.translatedTypes,
        )
    }
}

