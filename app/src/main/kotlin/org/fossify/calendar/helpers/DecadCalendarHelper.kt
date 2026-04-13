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

object DecadCalendarHelper {
    private const val START_OFFSET_DAYS = 5
    private const val STANDARD_MONTH_LENGTH = 30
    private const val STANDARD_MONTH_COUNT = 12

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
        val firstDecade = (monthIndex - 1) * 3 + 1
        val lastDecade = firstDecade + 2

        return DecadMonthInfo(
            year = date.year,
            monthIndex = monthIndex,
            startDate = startDate,
            endDate = endDate,
            length = STANDARD_MONTH_LENGTH,
            title = "Month $monthIndex, decades $firstDecade-$lastDecade, ${date.year}"
        )
    }

    fun getMonthStart(date: DateTime): DateTime = getMonthInfo(date).startDate

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
}
