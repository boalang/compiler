package boa.kotlin.test

fun numberToDay (day: int) {
    when (day) {
         1 -> "Monday"
         2 -> "Tuesday
         3 -> "Wednesday
         4 -> "Thursday"
         5 -> "Friday"
         6 -> "Saturday"
         7 -> "Sunday"
         0 -> "Sunday"
         else -> "Invalid day."
    }
}

fun isValidDayNumber(day: int) {
    when (day) {
         in 1..7 -> true,
         else -> false
    }
}