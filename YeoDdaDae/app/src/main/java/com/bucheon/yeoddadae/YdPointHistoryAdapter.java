package com.bucheon.yeoddadae;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.Timestamp;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class YdPointHistoryAdapter extends BaseAdapter {
    ArrayList<YdPointHistoryItem> items = new ArrayList<>();

    public void addItem(YdPointHistoryItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(YdPointHistoryItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    /*
    public YdPointHistoryItem findItem(CalendarDay date) {
        for (YdPointHistoryItem item : items) {
            if (item.getDate().equals(date)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

     */

    public void sortByUpTime() {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<YdPointHistoryItem>() {
                @Override
                public int compare(YdPointHistoryItem o1, YdPointHistoryItem o2) {
                    return o2.getUpTime().compareTo(o1.getUpTime()); // 역순으로 정렬
                }
            });

            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        YdPointHistoryItem item = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.yd_point_history_item, parent, false);
        }

        TextView priceTxt = convertView.findViewById(R.id.priceTxt);
        TextView typeTxt = convertView.findViewById(R.id.typeTxt);
        TextView additionType = convertView.findViewById(R.id.additionType);
        TextView upTimeTxt = convertView.findViewById(R.id.upTimeTxt);

        String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(item.getPoint());
        if (item.getType().equals("충전") || item.getType().equals("받음") ) {
            priceTxt.setTextColor(Color.rgb(128, 128 , 255));
            priceTxt.setText("+" + formattedYdPoint);

            if (item.getType().equals("충전")) {
                typeTxt.setText("충전");
                additionType.setVisibility(View.GONE);
            }
            else if (item.getType().equals("받음")) {
                typeTxt.setText("받음");
                additionType.setVisibility(View.VISIBLE);
                additionType.setText(item.getReceiveType());
            }
        }
        else {
            priceTxt.setTextColor(Color.rgb(255, 128 , 128));
            priceTxt.setText("-" + formattedYdPoint);

            if (item.getType().equals("환급")) {
                typeTxt.setText("환급");
                additionType.setVisibility(View.GONE);
            }
            else if (item.getType().equals("사용")) {
                typeTxt.setText("사용");
                additionType.setVisibility(View.VISIBLE);
                additionType.setText(item.getSpendType());
            }
        }

        Timestamp timestamp = item.getUpTime();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
            String dateString = sdf.format(date);
            upTimeTxt.setText(dateString);
        }

        return convertView;
    }
}
