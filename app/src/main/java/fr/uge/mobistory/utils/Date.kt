package fr.uge.mobistory.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import fr.uge.mobistory.R
import java.time.LocalDate

/**
 * Parses a string of the following format "date:year-month-day"
 * and returns a LocalDate.
 *
 * @param dateString String, the date in string.
 * @return LocalDate.
 */
fun toLocalDate(dateString: String): LocalDate {
    val date = dateString.substring("date:".length) // TODO degager le date: avant de mettre dans la bd (my bad)
    val split = date.split("-")
    val year: Int
    var month = 1
    var day = 1
    var index = 0
    year = if (split.size == 4) { // The year is negative
        index++
        ("-" + split[index]).toInt()
    } else {
        split[index].toInt()
    }
    index++
    if (split[index] != "0") {
        month = split[index].toInt()
    }
    index++
    if (split[index] != "0") {
        day = split[index].toInt()
    }
    return LocalDate.of(year, month, day)
}

/**
 * Formats two dates for the detailed event page.
 *
 * @param dateBeginString String, the begin date.
 * @param dateEndString String, the end date.
 * @return String, formatted string.
 */
@Composable
fun formatBeginAndEndDate(dateBeginString: String, dateEndString: String): String {
    if (compareTwoDatesString(dateBeginString, dateEndString)) {
        return toCorrectFormat(dateBeginString)
    }
    val beginDate = toCorrectFormat(dateBeginString)
    val endDate = toCorrectFormat(dateEndString)
    return LocalContext.current.getString(R.string.different_date, beginDate, endDate)
}

/**
 * Format a date according to the system language.
 *
 * @param dateString, the date in String.
 * @return String, a formatted String.
 */
@Composable
fun toCorrectFormat(date: String): String {
    //val date = dateString.substring("date:".length)
    val split = date.split("-")
    val year: String
    var month = ""
    var day = ""
    var index = 0
    if (split.size == 4) {
        index++
        return LocalContext.current.getString(R.string.bc, split[index])
    }
    year = split[index]
    index++
    if (split[index] != "0") {
        month = split[index]
    }
    index++
    if (split[index] != "0") {
        day = split[index]
    }
    if (day.isEmpty() && month.isEmpty()) {
        return LocalContext.current.getString(R.string.ac, year)
    }
    if (day.isEmpty()) {
        return LocalContext.current.getString(R.string.without_days, month, year)
    }
    return LocalContext.current.getString(R.string.full_date, day, month, year)
}

/**
 * Compares two strings and returns a boolean.
 *
 * @param dateBeginString String, the begin date.
 * @param dateEndString String, the end date.
 * @return Boolean, true if the date are the same, false if not.
 */
fun compareTwoDatesString(dateBeginString: String, dateEndString: String): Boolean {
    val beginDate = toLocalDate(dateBeginString)
    val endDate = toLocalDate(dateEndString)
    return beginDate.isEqual(endDate)
}