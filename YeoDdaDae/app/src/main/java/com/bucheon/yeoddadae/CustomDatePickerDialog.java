package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import org.threeten.bp.LocalDate;

import java.text.ParseException;

public class CustomDatePickerDialog extends Dialog {
    Context context;
    int startOrEnd;

    MaterialCalendarView customDatePicker;

    public CustomDatePickerDialog(@NonNull Context context, int startOrEnd) {
        super(context);
        this.context = context;
        this.startOrEnd = startOrEnd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_date_picker_dialog);

        customDatePicker = findViewById(R.id.customDatePicker);
        customDatePicker.setWeekDayFormatter(new ArrayWeekDayFormatter(context.getResources().getTextArray(R.array.custom_weekdays)));

        customDatePicker.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append(" ")
                        .append(calendarHeaderElements[1]);
                return calendarHeaderBuilder.toString();
            }
        });

        customDatePicker.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Log.d(TAG, "선택한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                String formattedDate = String.format("%04d%02d%02d", date.getYear(), date.getMonth(), date.getDay());

                if (context instanceof StatisticsActivity) {
                    StatisticsActivity sa = (StatisticsActivity) context;

                    if (startOrEnd == 0) {
                        try {
                            sa.receiveStartDateFromDialog(formattedDate);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (startOrEnd == 1) {
                        try {
                            sa.receiveEndDateFromDialog(formattedDate);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                dismiss();
            }
        });
    }
}
