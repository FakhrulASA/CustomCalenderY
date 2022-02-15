package com.example.calender

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class MainActivity : AppCompatActivity() {
    private var selectedDate: LocalDate? = null
    private var listOfDates = listOf(2,15,18,19,20,28,29)

    private lateinit var calenderView: CalendarView
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calenderView = findViewById(R.id.calendarView)
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: CalendarDay

            init {

                textView.setOnClickListener {
                    // Check the day owner as we do not want to select in or out dates.
                    if (day.owner == DayOwner.THIS_MONTH) {
                        // Keep a reference to any previous selection
                        // in case we overwrite it and need to reload it.
                        val currentSelection = selectedDate
                        if (currentSelection == day.date) {
                            // If the user clicks the same date, clear selection
                            selectedDate = null
                            // Reload this date so the dayBinder is called
                            // and we can REMOVE the selection background.
                            calenderView.notifyDateChanged(currentSelection)

                        } else {
                            selectedDate = day.date
                            // Reload the newly selected date so the dayBinder is
                            // called and we can ADD the selection background.
                            calenderView.notifyDateChanged(day.date)
                            if (currentSelection != null) {
                                // We need to also reload the previously selected
                                // date so we can REMOVE the selection background.
                                calenderView.notifyDateChanged(currentSelection)
                            }
                        }
                    }
                }
            }

            // With ViewBinding
            // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
        }



        calenderView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {

                container.day = day
                println(day.date.month.toString())
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                textView.alpha = if (day.date.dayOfMonth in listOfDates && day.date.month == Month.FEBRUARY) 1f else 0.3f
                if (day.date.dayOfMonth in listOfDates) {
                } else {
                    textView.isEnabled = false
                }
                if (day.owner == DayOwner.THIS_MONTH) {
                    // Show the month dates. Remember that views are recycled!
                    textView.visibility = View.VISIBLE
                    if (day.date == selectedDate) {
                        // If this is the selected date, show a round background and change the text color.
                        textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.round2)
                    } else {
                        // If this is NOT the selected date, remove the background and reset the text color.
                        textView.setTextColor(Color.BLACK)
                        textView.setBackgroundResource(R.drawable.round)
                    }
                } else {
                    // Hide in and out dates
                    textView.visibility = View.GONE
                }
            }

        }
        calenderView.setOnTouchListener { _, event ->
            event.action == MotionEvent.ACTION_MOVE
        }

        calenderView.updateMonthConfiguration(
            inDateStyle = InDateStyle.FIRST_MONTH,
            maxRowCount = 6,
            hasBoundaries = true
        )
        calenderView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.textView.text =
                    "${month.yearMonth.month.name.toLowerCase().capitalize()} ${month.year}"
            }
        }
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calenderView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calenderView.scrollToMonth(currentMonth)


    }

    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek is not DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            return rhs + lhs
        }
        return daysOfWeek
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.action == MotionEvent.ACTION_MOVE) true else super.dispatchTouchEvent(ev)
    }
}