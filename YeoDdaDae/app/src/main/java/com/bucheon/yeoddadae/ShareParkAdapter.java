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
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ShareParkAdapter extends BaseAdapter {
    ArrayList<ShareParkItem> items = new ArrayList<>();
    Activity activity;
    Calendar ca = Calendar.getInstance();

    public ShareParkAdapter (Activity activity) {
        this.activity = activity;
    }

    public void addItem(ShareParkItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(ShareParkItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void clearItem() {
        items.clear();
        notifyDataSetChanged();
    }

    public ShareParkItem findItem(String documentId) {
        for (ShareParkItem item : items) {
            if (item.getDocumentId().equals(documentId)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
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
        ShareParkItem sharePark = items.get(position);

        if (activity instanceof MainActivity) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.share_park_item, parent, false);
            }

            TextView shareParkLocationTxt = convertView.findViewById(R.id.shareParkLocationTxt);
            TextView shareParkShareTimeTxt = convertView.findViewById(R.id.shareParkShareTimeTxt);
            TextView shareParkIsCancelledTxt = convertView.findViewById(R.id.shareParkIsCancelledTxt);
            TextView shareParkPriceTxt = convertView.findViewById(R.id.shareParkPriceTxt);
            TextView shareParkUpTimeTxt = convertView.findViewById(R.id.shareParkUpTimeTxt);

            TMapData tMapdata = new TMapData();
            tMapdata.reverseGeocoding(sharePark.getLat(), sharePark.getLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    if (tMapAddressInfo != null) {
                        String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                        String address = adrresses[2] + " / " + sharePark.getParkDetailAddress();
                        // UI 업데이트를 메인(UI) 스레드로 보내기
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                shareParkLocationTxt.setText(address);
                            }
                        });
                    }
                }
            });

            HashMap<String, ArrayList<String>> hm = sharePark.getTime();
            ArrayList<String> keys = new ArrayList<>(hm.keySet());
            Collections.sort(keys);

            String reservationTimeString = "";
            for (String key : keys) {
                int year = Integer.parseInt(key.substring(0, 4));
                int month = Integer.parseInt(key.substring(4, 6));
                int day = Integer.parseInt(key.substring(6, 8));

                String startTimeHour = hm.get(key).get(0).substring(0,2);
                String startTimeMinute = hm.get(key).get(0).substring(2,4);
                String endTimeHour = hm.get(key).get(1).substring(0,2);
                String endTimeMinute = hm.get(key).get(1).substring(2,4);

                reservationTimeString += year + "년 " + month + "월 " + day + "일 " + startTimeHour + ":" + startTimeMinute + "부터 " + endTimeHour + ":" + endTimeMinute + "까지\n";
            }
            if (!reservationTimeString.isEmpty()) {
                reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거
            }
            shareParkShareTimeTxt.setText(reservationTimeString);

            int statusColor = shareParkIsCancelledTxt.getCurrentTextColor();

            if (sharePark.isCancelled()) {
                shareParkIsCancelledTxt.setText("취소됨");
                statusColor = Color.rgb(255, 0, 0);
                shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
            }
            else if (sharePark.isCalculated()) {
                shareParkIsCancelledTxt.setText("정산됨");
                statusColor = Color.rgb(0, 128, 0);
                shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
            }
            else if (sharePark.isApproval()) {
                int year = ca.get(Calendar.YEAR);
                int month = ca.get(Calendar.MONTH) + 1;
                int day = ca.get(Calendar.DAY_OF_MONTH);
                int hour = ca.get(Calendar.HOUR_OF_DAY);
                int minute = ca.get(Calendar.MINUTE);
                String nowString = "";
                nowString += year;
                if (month < 10) {
                    nowString += "0";
                }
                nowString += month;
                if (day < 10) {
                    nowString += "0";
                }
                nowString += day;
                if (hour < 10) {
                    nowString += "0";
                }
                nowString += hour;
                if (minute < 10) {
                    nowString += "0";
                }
                nowString += minute;

                HashMap<String, ArrayList<String>> shareTIme = sharePark.getTime();
                List<String> sortedKeys = new ArrayList<>(shareTIme.keySet());
                Collections.sort(sortedKeys);
                String firstTime = sortedKeys.get(0) + shareTIme.get(sortedKeys.get(0)).get(0);
                String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTIme.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                if (nowString.compareTo(firstTime) < 0) {
                    shareParkIsCancelledTxt.setText("승인됨");
                    statusColor = Color.rgb(0, 0, 255);
                    shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
                }
                else if (nowString.compareTo(endTime) <= 0){
                    shareParkIsCancelledTxt.setText("사용중");
                    statusColor = Color.rgb(0, 0, 255);
                    shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
                }
                else {
                    shareParkIsCancelledTxt.setText("정산 대기 중");
                    statusColor = Color.rgb(32, 200, 32);
                    shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
                }
            }
            else {
                int year = ca.get(Calendar.YEAR);
                int month = ca.get(Calendar.MONTH) + 1;
                int day = ca.get(Calendar.DAY_OF_MONTH);
                int hour = ca.get(Calendar.HOUR_OF_DAY);
                int minute = ca.get(Calendar.MINUTE);
                String nowString = "";
                nowString += year;
                if (month < 10) {
                    nowString += "0";
                }
                nowString += month;
                if (day < 10) {
                    nowString += "0";
                }
                nowString += day;
                if (hour < 10) {
                    nowString += "0";
                }
                nowString += hour;
                if (minute < 10) {
                    nowString += "0";
                }
                nowString += minute;

                HashMap<String, ArrayList<String>> shareTime = sharePark.getTime();
                List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                Collections.sort(sortedKeys);
                String firstTime = sortedKeys.get(0) + shareTime.get(sortedKeys.get(0)).get(0);

                if (nowString.compareTo(firstTime) < 0){
                    shareParkIsCancelledTxt.setText("승인 대기 중");
                    statusColor = shareParkUpTimeTxt.getCurrentTextColor();
                    shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
                }
                else {
                    shareParkIsCancelledTxt.setText("승인 실패");
                    statusColor = Color.rgb(255, 0, 0);
                    shareParkIsCancelledTxt.setVisibility(View.VISIBLE);
                }
            }
            shareParkIsCancelledTxt.setTextColor(statusColor);


            if (sharePark.getPrice() == 0) {
                shareParkPriceTxt.setText("무료");
            }
            else {
                shareParkPriceTxt.setText("시간 당 " + sharePark.getPrice() + "pt");
            }

            Timestamp timestamp = sharePark.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                shareParkUpTimeTxt.setText(dateString);
            }
        }
        else if (context instanceof ApproveShareParkActivity) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.approve_share_park_item, parent, false);
            }

            TextView approveShareParkLocationTxt = convertView.findViewById(R.id.approveShareParkLocationTxt);
            TextView approveShareParkShareTimeTxt = convertView.findViewById(R.id.approveShareParkShareTimeTxt);
            TextView approveShareParkOwnerIdTxt = convertView.findViewById(R.id.approveShareParkOwnerIdTxt);
            TextView approveShareParkPriceTxt = convertView.findViewById(R.id.approveShareParkPriceTxt);
            TextView approveShareParkUpTimeTxt = convertView.findViewById(R.id.approveShareParkUpTimeTxt);

            TMapData tMapdata = new TMapData();
            tMapdata.reverseGeocoding(sharePark.getLat(), sharePark.getLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    if (tMapAddressInfo != null) {
                        String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                        String address = adrresses[2] + " / " + sharePark.getParkDetailAddress();
                        // UI 업데이트를 메인(UI) 스레드로 보내기
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                approveShareParkLocationTxt.setText(address);
                            }
                        });
                    }
                }
            });

            HashMap<String, ArrayList<String>> hm = sharePark.getTime();
            ArrayList<String> keys = new ArrayList<>(hm.keySet());
            Collections.sort(keys);

            String reservationTimeString = "";
            for (String key : keys) {
                int year = Integer.parseInt(key.substring(0, 4));
                int month = Integer.parseInt(key.substring(4, 6));
                int day = Integer.parseInt(key.substring(6, 8));

                String startTimeHour = hm.get(key).get(0).substring(0,2);
                String startTimeMinute = hm.get(key).get(0).substring(2,4);
                String endTimeHour = hm.get(key).get(1).substring(0,2);
                String endTimeMinute = hm.get(key).get(1).substring(2,4);

                reservationTimeString += year + "년 " + month + "월 " + day + "일 " + startTimeHour + ":" + startTimeMinute + "부터 " + endTimeHour + ":" + endTimeMinute + "까지\n";
            }
            if(!reservationTimeString.isEmpty()) {
                reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거
            }
            approveShareParkShareTimeTxt.setText(reservationTimeString);

            approveShareParkOwnerIdTxt.setText(sharePark.getOwnerId());

            if (sharePark.getPrice() == 0) {
                approveShareParkPriceTxt.setText("무료");
            }
            else {
                approveShareParkPriceTxt.setText("시간 당 " + sharePark.getPrice() + "원");
            }

            Timestamp timestamp = sharePark.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                approveShareParkUpTimeTxt.setText(dateString);
            }
        }
        return convertView;
    }
}
