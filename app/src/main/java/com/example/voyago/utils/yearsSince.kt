package com.example.voyago.utils

import android.util.Log
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun yearsSince(dateString: String): Int {
    val today = LocalDate.now()

    return try {
        val fullDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val fullDate = LocalDate.parse(dateString, fullDateFormatter)
        Period.between(fullDate, today).years
    } catch (e1: DateTimeParseException) {
        try {
            val yearOnly = dateString.toInt()
            val date = LocalDate.of(yearOnly, 1, 1)
            Period.between(date, today).years
        } catch (e2: Exception) {
            Log.e("yearsSince", "Invalid date string: $dateString", e2)
            0
        }
    }
}

