package org.fossify.calendar.helpers

import android.content.Context
import org.fossify.calendar.extensions.config
import org.fossify.calendar.extensions.seconds
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

object Formatter {
    const val DAYCODE_PATTERN = "YYYYMMdd"
    const val YEAR_PATTERN = "YYYY"
    const val TIME_PATTERN = "HHmmss"
    private const val MONTH_PATTERN = "MMM"
    private const val DAY_PATTERN = "d"

    fun getDateFromCode(context: Context, dayCode: String, shortMonth: Boolean = false): String {
        val dateTime = getDateTimeFromCode(dayCode)
        val decimalDate = DecadCalendarHelper.getDecimalDate(dateTime)
        val day = decimalDate?.day?.toString() ?: dateTime.toString(DAY_PATTERN)
        val year = dateTime.toString(YEAR_PATTERN)
        val monthIndex = decimalDate?.month ?: Integer.valueOf(dayCode.substring(4, 6))
        var month = getMonthName(context, monthIndex)
        if (shortMonth) {
            month = month.substring(0, Math.min(month.length, 3))
        }

        var date = "$month $day"
        if (year != DateTime().toString(YEAR_PATTERN)) {
            date += " $year"
        }

        return date
    }

    fun getDayTitle(context: Context, dayCode: String, addDayOfWeek: Boolean = true): String {
        val date = getDateFromCode(context, dayCode)
        val dateTime = getDateTimeFromCode(dayCode)
        val day = DecadCalendarHelper.getDecadeDayShortName(dateTime)
        return if (addDayOfWeek)
            "$date ($day)"
        else
            date
    }

    fun getDateDayTitle(dayCode: String): String {
        val dateTime = getDateTimeFromCode(dayCode)
        return "${dateTime.toString(DAY_PATTERN)} ${DecadCalendarHelper.getDecadeDayName(dateTime)}"
    }

    fun getLongMonthYear(context: Context, dayCode: String): String {
        val dateTime = getDateTimeFromCode(dayCode)
        val decimalDate = DecadCalendarHelper.getDecimalDate(dateTime)
        val monthIndex = decimalDate?.month ?: Integer.valueOf(dayCode.substring(4, 6))
        val month = getMonthName(context, monthIndex)
        val year = dateTime.toString(YEAR_PATTERN)
        var date = month

        if (year != DateTime().toString(YEAR_PATTERN)) {
            date += " $year"
        }

        return date
    }

    fun getDate(context: Context, dateTime: DateTime, addDayOfWeek: Boolean = true) = getDayTitle(context, getDayCodeFromDateTime(dateTime), addDayOfWeek)

    fun getFullDate(context: Context, dateTime: DateTime): String {
        val decimalDate = DecadCalendarHelper.getDecimalDate(dateTime)
        val day = decimalDate?.day?.toString() ?: dateTime.toString(DAY_PATTERN)
        val year = dateTime.toString(YEAR_PATTERN)
        val monthIndex = decimalDate?.month ?: dateTime.monthOfYear
        val month = getMonthName(context, monthIndex)
        return "$month $day $year"
    }

    fun getTodayCode() = getDayCodeFromTS(getNowSeconds())

    fun getTodayDayNumber() = getDateTimeFromTS(getNowSeconds()).toString(DAY_PATTERN)

    fun getCurrentMonthShort() = getDateTimeFromTS(getNowSeconds()).toString(MONTH_PATTERN)

    fun getTime(context: Context, dateTime: DateTime): String {
        val decimalSecondOfDay = dateTime.millisOfDay.toLong() * 100_000L / (24L * 60L * 60L * 1000L)
        val hour = (decimalSecondOfDay / 10_000).toInt()
        val minute = ((decimalSecondOfDay / 100) % 100).toInt()
        return "%02d:%02d".format(hour, minute)
    }

    fun getDateTimeFromCode(dayCode: String) = DateTimeFormat.forPattern(DAYCODE_PATTERN).withZone(DateTimeZone.UTC).parseDateTime(dayCode)

    fun getLocalDateTimeFromCode(dayCode: String) =
        DateTimeFormat.forPattern(DAYCODE_PATTERN).withZone(DateTimeZone.getDefault()).parseLocalDate(dayCode).toDateTimeAtStartOfDay()

    fun getTimeFromTS(context: Context, ts: Long) = getTime(context, getDateTimeFromTS(ts))

    fun getDayStartTS(dayCode: String) = getLocalDateTimeFromCode(dayCode).seconds()

    fun getDayEndTS(dayCode: String) = getLocalDateTimeFromCode(dayCode).plusDays(1).minusMinutes(1).seconds()

    fun getDayCodeFromDateTime(dateTime: DateTime) = dateTime.toString(DAYCODE_PATTERN)

    fun getDateFromTS(ts: Long) = LocalDate(ts * 1000L, DateTimeZone.getDefault())

    fun getDateTimeFromTS(ts: Long, tz: DateTimeZone = DateTimeZone.getDefault()) = DateTime(ts * 1000L, tz)

    fun getUTCDateTimeFromTS(ts: Long) = DateTime(ts * 1000L, DateTimeZone.UTC)

    // use manually translated month names, as DateFormat and Joda have issues with a lot of languages
    fun getMonthName(context: Context, id: Int) = context.resources.getStringArray(org.fossify.commons.R.array.months)[id - 1]

    fun getShortMonthName(context: Context, id: Int) = context.resources.getStringArray(org.fossify.commons.R.array.months_short)[id - 1]

    fun getHourPattern(context: Context) = "HH"

    fun getTimePattern(context: Context) = "HH:mm"

    fun getExportedTime(ts: Long): String {
        val dateTime = DateTime(ts, DateTimeZone.UTC)
        return "${dateTime.toString(DAYCODE_PATTERN)}T${dateTime.toString(TIME_PATTERN)}Z"
    }

    fun getDayCodeFromTS(ts: Long, tz: DateTimeZone = DateTimeZone.getDefault()): String {
        val daycode = getDateTimeFromTS(ts, tz).toString(DAYCODE_PATTERN)
        return if (daycode.isNotEmpty()) {
            daycode
        } else {
            "0"
        }
    }

    fun getUTCDayCodeFromTS(ts: Long) = getUTCDateTimeFromTS(ts).toString(DAYCODE_PATTERN)

    fun getYearFromDayCode(dayCode: String) = getDateTimeFromCode(dayCode).toString(YEAR_PATTERN)

    fun getShiftedTS(dateTime: DateTime, toZone: DateTimeZone) = dateTime.withTimeAtStartOfDay().withZoneRetainFields(toZone).seconds()

    fun getShiftedLocalTS(ts: Long) = getShiftedTS(dateTime = getUTCDateTimeFromTS(ts), toZone = DateTimeZone.getDefault())

    fun getShiftedUtcTS(ts: Long) = getShiftedTS(dateTime = getDateTimeFromTS(ts), toZone = DateTimeZone.UTC)
}
