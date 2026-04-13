package org.fossify.calendar.helpers

import org.joda.time.DateTime

data class DecadMonthInfo(
    val year: Int,
    val monthIndex: Int,
    val startDate: DateTime,
    val endDate: DateTime,
    val length: Int,
    val title: String
)

data class DecimalDate(
    val month: Int,
    val day: Int
)

object DecadCalendarHelper {
    const val DAYS_IN_DECADE = 10
    const val HOURS_IN_DAY = 10
    const val MINUTES_IN_HOUR = 100
    const val SECONDS_IN_MINUTE = 100

    private val DECADE_DAY_NAMES = listOf(
        "Понедельник",
        "Вторник",
        "Среда",
        "Четверг",
        "Пятница",
        "Суббота",
        "Седьмица",
        "Осьмик",
        "Девятица",
        "Неделя"
    )

    private val DECADE_DAY_SHORT_NAMES = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Сд", "Ос", "Дв", "Нд")

    private const val START_OFFSET_DAYS = 5
    private const val STANDARD_MONTH_LENGTH = 30
    private const val STANDARD_MONTH_COUNT = 12
    private val DAYS_BEFORE_MONTH_COMMON = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    private val DAYS_BEFORE_MONTH_LEAP = intArrayOf(0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335)
    private val DAYS_IN_MONTH_COMMON = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val DAYS_IN_MONTH_LEAP = intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    fun getMonthInfo(date: DateTime): DecadMonthInfo {
        val yearStart = date.withDayOfYear(1).withTimeAtStartOfDay()
        val dayOfYear = date.dayOfYear
        val leapExtra = if (date.year().isLeap) 1 else 0
        val specialDaysLength = START_OFFSET_DAYS + leapExtra

        if (dayOfYear <= specialDaysLength) {
            return DecadMonthInfo(
                year = date.year,
                monthIndex = 0,
                startDate = yearStart,
                endDate = yearStart.plusDays(specialDaysLength - 1),
                length = specialDaysLength,
                title = "Special days ${date.year}"
            )
        }

        val shiftedDay = dayOfYear - specialDaysLength - 1
        val monthIndex = shiftedDay / STANDARD_MONTH_LENGTH + 1
        val monthStartDayOfYear = specialDaysLength + 1 + (monthIndex - 1) * STANDARD_MONTH_LENGTH
        val startDate = yearStart.withDayOfYear(monthStartDayOfYear)
        val endDate = startDate.plusDays(STANDARD_MONTH_LENGTH - 1)
        return DecadMonthInfo(
            year = date.year,
            monthIndex = monthIndex,
            startDate = startDate,
            endDate = endDate,
            length = STANDARD_MONTH_LENGTH,
            title = "Month $monthIndex, ${date.year}"
        )
    }

    fun getMonthStart(date: DateTime): DateTime = getMonthInfo(date).startDate

    fun getMonthInfo(year: Int, monthIndex: Int): DecadMonthInfo {
        val isLeap = DateTime(year, 1, 1, 0, 0).year().isLeap
        val specialDaysLength = START_OFFSET_DAYS + if (isLeap) 1 else 0
        val yearStart = DateTime(year, 1, 1, 0, 0).withTimeAtStartOfDay()
        val startDayOfYear = specialDaysLength + 1 + (monthIndex - 1) * STANDARD_MONTH_LENGTH
        val startDate = yearStart.withDayOfYear(startDayOfYear)
        val endDate = startDate.plusDays(STANDARD_MONTH_LENGTH - 1)
        return DecadMonthInfo(
            year = year,
            monthIndex = monthIndex,
            startDate = startDate,
            endDate = endDate,
            length = STANDARD_MONTH_LENGTH,
            title = "Month $monthIndex, $year"
        )
    }

    fun getPreviousMonthStart(date: DateTime): DateTime {
        val currentStart = getMonthStart(date)
        return getMonthInfo(currentStart.minusDays(1)).startDate
    }

    fun getNextMonthStart(date: DateTime): DateTime {
        val currentInfo = getMonthInfo(date)
        return getMonthInfo(currentInfo.endDate.plusDays(1)).startDate
    }

    fun getMonthKey(date: DateTime): String {
        val info = getMonthInfo(date)
        return "${info.year}-${info.monthIndex}"
    }

    fun getDecadeDayIndex(date: DateTime): Int {
        val monthInfo = getMonthInfo(date)
        val daysFromMonthStart = ((date.withTimeAtStartOfDay().millis - monthInfo.startDate.millis) / (24 * 60 * 60 * 1000L)).toInt()
        return ((daysFromMonthStart % DAYS_IN_DECADE) + DAYS_IN_DECADE) % DAYS_IN_DECADE
    }

    fun getDecadeDayName(date: DateTime): String = DECADE_DAY_NAMES[getDecadeDayIndex(date)]

    fun getDecadeDayShortName(date: DateTime): String = DECADE_DAY_SHORT_NAMES[getDecadeDayIndex(date)]

    fun getDecadeDayShortNames(): List<String> = DECADE_DAY_SHORT_NAMES

    fun getDecadeNumber(date: DateTime): Int? {
        val leapExtra = if (date.year().isLeap) 1 else 0
        val specialDaysLength = START_OFFSET_DAYS + leapExtra
        val shiftedDayOfYear = date.dayOfYear - specialDaysLength
        if (shiftedDayOfYear <= 0) {
            return null
        }

        return ((shiftedDayOfYear - 1) / DAYS_IN_DECADE) + 1
    }

    fun getDecimalDate(date: DateTime): DecimalDate? {
        val isLeap = date.year().isLeap
        val shift = if (isLeap) START_OFFSET_DAYS + 1 else START_OFFSET_DAYS
        val dayOfYear = date.dayOfMonth + getDaysBeforeMonth(date.monthOfYear, isLeap)
        val decimalDayOfYear = dayOfYear - shift

        if (decimalDayOfYear <= 0) {
            return null
        }

        return convertFromDayOfYear(decimalDayOfYear, isLeap)
    }

    private fun getDaysBeforeMonth(month: Int, isLeap: Boolean): Int {
        val daysBeforeMonth = if (isLeap) DAYS_BEFORE_MONTH_LEAP else DAYS_BEFORE_MONTH_COMMON
        return daysBeforeMonth[month - 1]
    }

    private fun convertFromDayOfYear(dayOfYear: Int, isLeap: Boolean): DecimalDate {
        val daysInMonth = if (isLeap) DAYS_IN_MONTH_LEAP else DAYS_IN_MONTH_COMMON
        var remainingDays = dayOfYear
        var month = 1

        while (month <= 12) {
            val monthLength = daysInMonth[month - 1]
            if (remainingDays <= monthLength) {
                return DecimalDate(month = month, day = remainingDays)
            }

            remainingDays -= monthLength
            month++
        }

        return DecimalDate(month = 12, day = daysInMonth.last())
    }

    fun getDecimalTime(hourOfDay: Int, minuteOfHour: Int): Pair<Int, Int> {
        val totalMillis = (hourOfDay * 60L + minuteOfHour) * 60_000L
        val decimalSecondOfDay = totalMillis * 100_000L / (24L * 60L * 60L * 1000L)
        val hour = (decimalSecondOfDay / 10_000).toInt()
        val minute = ((decimalSecondOfDay / 100) % 100).toInt()
        return hour to minute
    }

    fun getStandardTimeFromDecimal(decimalHour: Int, decimalMinute: Int): Pair<Int, Int> {
        val boundedHour = decimalHour.coerceIn(0, HOURS_IN_DAY - 1)
        val boundedMinute = decimalMinute.coerceIn(0, MINUTES_IN_HOUR - 1)
        val decimalSecondOfDay = boundedHour * 10_000L + boundedMinute * 100L
        val millisOfDay = decimalSecondOfDay * (24L * 60L * 60L * 1000L) / 100_000L
        val totalMinutes = (millisOfDay / 60_000L).toInt()
        val hour = totalMinutes / 60
        val minute = totalMinutes % 60
        return hour to minute
    }
}
