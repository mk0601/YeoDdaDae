package com.bucheon.yeoddadae;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class EnableSpecificDaysDecorator implements DayViewDecorator {
    private final HashSet<CalendarDay> dates;

    public EnableSpecificDaysDecorator(Collection<CalendarDay> dates) {
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day); // 설정된 날짜들만 활성화
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setDaysDisabled(false); // 선택 가능하도록 설정
    }
}