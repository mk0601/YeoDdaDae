package com.bucheon.yeoddadae;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class DisableAllDaysDecorator implements DayViewDecorator {
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true; // 모든 날짜에 대해 적용
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setDaysDisabled(true); // 모든 날짜를 비활성화
    }
}