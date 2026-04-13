package org.fossify.calendar.helpers

import android.content.Context
import org.fossify.calendar.extensions.eventsHelper
import org.fossify.calendar.extensions.getProperDayIndexInWeek
import org.fossify.calendar.extensions.isWeekendIndex
import org.fossify.calendar.extensions.seconds
import org.fossify.calendar.interfaces.MonthlyCalendar
import org.fossify.calendar.models.DayMonthly
import org.fossify.calendar.models.Event
import org.joda.time.DateTime

class MonthlyCalendarImpl(val callback: MonthlyCalendar, val context: Context) {
    private val DAYS_CNT = ROW_COUNT * COLUMN_COUNT

    private val mToday: String = DateTime().toString(Formatter.DAYCODE_PATTERN)
    private var mEvents = ArrayList<Event>()

    lateinit var mTargetDate: DateTime

    fun updateMonthlyCalendar(targetDate: DateTime) {
        mTargetDate = targetDate
        val targetInfo = DecadCalendarHelper.getMonthInfo(mTargetDate)
        val startTS = targetInfo.startDate.minusDays(COLUMN_COUNT).seconds()
        val endTS = targetInfo.endDate.plusDays(COLUMN_COUNT).seconds()
        context.eventsHelper.getEvents(startTS, endTS) {
            gotEvents(it)
        }
    }

    fun getMonth(targetDate: DateTime) {
        updateMonthlyCalendar(targetDate)
    }

    fun getDays(markDaysWithEvents: Boolean) {
        val days = ArrayList<DayMonthly>(DAYS_CNT)
        val currentMonth = DecadCalendarHelper.getMonthInfo(mTargetDate)
        val previousMonth = DecadCalendarHelper.getMonthInfo(currentMonth.startDate.minusDays(1))

        val firstDayIndex = context.getProperDayIndexInWeek(currentMonth.startDate)
        val currMonthDays = currentMonth.length
        val prevMonthDays = previousMonth.length

        var value = prevMonthDays - firstDayIndex + 1
        var isThisMonth = false
        var cursorDate = previousMonth.startDate.plusDays(value - 1)

        for (i in 0 until DAYS_CNT) {
            when {
                i < firstDayIndex -> {
                    isThisMonth = false
                    cursorDate = previousMonth.startDate.plusDays(value - 1)
                }

                i == firstDayIndex -> {
                    value = 1
                    isThisMonth = true
                    cursorDate = currentMonth.startDate
                }

                value == currMonthDays + 1 -> {
                    value = 1
                    isThisMonth = false
                    cursorDate = currentMonth.endDate.plusDays(1)
                }
            }

            val newDay = if (isThisMonth) currentMonth.startDate.plusDays(value - 1) else cursorDate
            val dayCode = Formatter.getDayCodeFromDateTime(newDay)
            val day = DayMonthly(value, isThisMonth, isToday(newDay), dayCode, newDay.weekOfWeekyear, ArrayList(), i, context.isWeekendIndex(i))
            days.add(day)

            value++
            cursorDate = cursorDate.plusDays(1)
        }

        if (markDaysWithEvents) {
            markDaysWithEvents(days)
        } else {
            callback.updateMonthlyCalendar(context, monthName, days, false, currentMonth.startDate)
        }
    }

    // it works more often than not, don't touch
    private fun markDaysWithEvents(days: ArrayList<DayMonthly>) {
        val dayEvents = HashMap<String, ArrayList<Event>>()
        mEvents.forEach { event ->
            val startDateTime = Formatter.getDateTimeFromTS(event.startTS)
            val endDateTime = Formatter.getDateTimeFromTS(event.endTS)
            val endCode = Formatter.getDayCodeFromDateTime(endDateTime)

            var currDay = startDateTime
            var dayCode = Formatter.getDayCodeFromDateTime(currDay)
            var currDayEvents = dayEvents[dayCode] ?: ArrayList()
            currDayEvents.add(event)
            dayEvents[dayCode] = currDayEvents

            while (Formatter.getDayCodeFromDateTime(currDay) != endCode) {
                currDay = currDay.plusDays(1)
                dayCode = Formatter.getDayCodeFromDateTime(currDay)
                currDayEvents = dayEvents[dayCode] ?: ArrayList()
                currDayEvents.add(event)
                dayEvents[dayCode] = currDayEvents
            }
        }

        days.filter { dayEvents.keys.contains(it.code) }.forEach {
            it.dayEvents = dayEvents[it.code]!!
        }
        callback.updateMonthlyCalendar(context, monthName, days, true, DecadCalendarHelper.getMonthInfo(mTargetDate).startDate)
    }

    private fun isToday(targetDate: DateTime): Boolean {
        return targetDate.toString(Formatter.DAYCODE_PATTERN) == mToday
    }

    private val monthName: String
        get() = DecadCalendarHelper.getMonthInfo(mTargetDate).title

    private fun gotEvents(events: ArrayList<Event>) {
        mEvents = events
        getDays(true)
    }
}
