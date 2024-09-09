package com.bucheon.yeoddadae;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ReportDiscountParkAdapter extends BaseAdapter {
    ArrayList<ReportDiscountParkItem> items = new ArrayList<>();
    Activity activity;

    public ReportDiscountParkAdapter(Activity activity) {
        this.activity = activity;
    }

    public void addItem(ReportDiscountParkItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public ReportDiscountParkItem findItem(String reporterId, Timestamp upTime) {
        for (ReportDiscountParkItem item : items) {
            if (item.getReporterId().equals(reporterId) && item.getUpTime().equals(upTime)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void clearItem () {
        items.clear();
        notifyDataSetChanged();
    }

    public void sortByUpTime() {
        Collections.sort(items, new Comparator<ReportDiscountParkItem>() {
            @Override
            public int compare(ReportDiscountParkItem o1, ReportDiscountParkItem o2) {
                return o1.getUpTime().compareTo(o2.getUpTime());
            }
        });
        notifyDataSetChanged();
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
        ReportDiscountParkItem report = items.get(position);

        if (activity instanceof MainActivity) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.my_report_discount_park_item, parent, false);
            }

            // 파인드 뷰
            TextView reportDiscountParkAddressTxt = convertView.findViewById(R.id.reportDiscountParkAddressTxt);
            TextView reportDiscountParkConditionAndDiscountTxt = convertView.findViewById(R.id.reportDiscountParkConditionAndDiscountTxt);
            TextView reportDiscountParkRateTxt = convertView.findViewById(R.id.reportDiscountParkRateTxt);
            TextView reportDiscountParkIsCancelledTxt = convertView.findViewById(R.id.reportDiscountParkIsCancelledTxt);
            TextView reportDiscountParkUpTimeTxt = convertView.findViewById(R.id.reportDiscountParkUpTimeTxt);

            reportDiscountParkAddressTxt.setText(report.getParkName());

            String conditionAndDiscount = report.getCondition();
            if (report.getDiscount() == 0) {
                conditionAndDiscount += " / 무료";
            }
            else {
                String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(report.getDiscount());
                conditionAndDiscount += " / " + formattedYdPoint + "원 할인";
            }
            reportDiscountParkConditionAndDiscountTxt.setText(conditionAndDiscount);

            String rate = report.getRatePerfectCount() + " / " + report.getRateMistakeCount() + " / " + report.getRateWrongCount();
            reportDiscountParkRateTxt.setText(rate);

            if (report.isCancelled) {
                reportDiscountParkIsCancelledTxt.setVisibility(View.VISIBLE);
            }
            else if (report.isApproval){
                reportDiscountParkIsCancelledTxt.setText("승인됨");
                reportDiscountParkIsCancelledTxt.setTextColor(Color.rgb(0, 0, 255));
                reportDiscountParkIsCancelledTxt.setVisibility(View.VISIBLE);
            }
            else {
                reportDiscountParkIsCancelledTxt.setVisibility(View.GONE);
            }

            Timestamp timestamp = report.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                reportDiscountParkUpTimeTxt.setText(dateString);
            }
        }

        else if (activity instanceof AnotherReportDiscountParkActivity
                || activity instanceof ApproveReportActivity) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.report_discount_park_item, parent, false);
            }

            // 파인드 뷰
            TextView reportDiscountParkNameTxt = convertView.findViewById(R.id.reportDiscountParkNameTxt);
            TextView reportDiscountParkConditionAndDiscountTxt = convertView.findViewById(R.id.reportDiscountParkConditionAndDiscountTxt);
            TextView reportDiscountParkRateTxt = convertView.findViewById(R.id.reportDiscountParkRateTxt);
            TextView reportDiscountParkIDTxt = convertView.findViewById(R.id.reportDiscountParkIDTxt);
            TextView reportDiscountParkUpTimeTxt = convertView.findViewById(R.id.reportDiscountParkUpTimeTxt);

            reportDiscountParkNameTxt.setText(report.getParkName());

            String conditionAndDiscount = report.getCondition();
            if (report.getDiscount() == 0) {
                conditionAndDiscount += " / 무료";
            }
            else {
                String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(report.getDiscount());
                conditionAndDiscount += " / " + formattedYdPoint + "원 할인";
            }
            reportDiscountParkConditionAndDiscountTxt.setText(conditionAndDiscount);

            String rate = report.getRatePerfectCount() + " / " + report.getRateMistakeCount() + " / " + report.getRateWrongCount();
            reportDiscountParkRateTxt.setText(rate);

            reportDiscountParkIDTxt.setText(report.getReporterId());

            Timestamp timestamp = report.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                reportDiscountParkUpTimeTxt.setText(dateString);
            }
        }

        return convertView;
    }
}
