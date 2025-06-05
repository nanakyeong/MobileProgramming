package com.example.mobileprogramming;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ReadDayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> readDates;

    public ReadDayDecorator(List<Long> readMillisList) {
        readDates = new HashSet<>();
        for (Long millis : readMillisList) {
            Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Seoul"));
            calendar.setTimeInMillis(millis);
            CalendarDay day = CalendarDay.from(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            readDates.add(day);
        }
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return readDates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFEB3B"))); // 노란 배경
    }
}