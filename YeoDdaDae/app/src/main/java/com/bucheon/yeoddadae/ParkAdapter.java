package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ParkAdapter extends BaseAdapter {
    ArrayList<ParkItem> items = new ArrayList<ParkItem>();

    public void addItem(ParkItem item) {
        items.add(item);
    }

    public ParkItem findItem(String parkItemName) {
        for (ParkItem item : items) {
            if (item.getName().equals(parkItemName)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public int getSize() {
        return items.size();
    }

    public void poiOverReport() {
        if (items == null || items.size() < 2) {
            return;
        }

        ArrayList<ParkItem> itemsToRemove = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ParkItem item1 = items.get(i);
            for (int j = i + 1; j < items.size(); j++) {
                ParkItem item2 = items.get(j);
                if ((item1.getPoiId() != null) && (item2.getPoiId() != null) && (item1.getPoiId().equals(item2.getPoiId()))) {
                    if (item1.getType() == 6 && item2.getType() == 6) {
                        Log.d(TAG, "한 poi에 제보가 2개 이상임");
                        continue;
                    }
                    if (item1.getType() == 6) {
                        itemsToRemove.add(item2);
                    }
                    else if (item2.getType() == 6) {
                        itemsToRemove.add(item1);
                    }
                }
            }
        }
        items.removeAll(itemsToRemove);
        notifyDataSetChanged();
    }

    public void sortByDistance () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<ParkItem>() {
                @Override
                public int compare(ParkItem o1, ParkItem o2) {
                    // Compare by star rate in descending order
                    return Double.compare(Double.parseDouble(o1.getRadius()), Double.parseDouble(o2.getRadius()));
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public void sortByParkPrice() {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<ParkItem>() {
                @Override
                public int compare(ParkItem o1, ParkItem o2) {
                    String price1 = o1.getParkPrice();
                    String price2 = o2.getParkPrice();

                    if (price1 == null && price2 == null) {
                        return 0; // Both are null, they are equal
                    }
                    if (price1 == null) {
                        return 1;
                    }
                    if (price2 == null) {
                        return -1;
                    }

                    return Long.compare(Long.parseLong(price1), Long.parseLong(price2)); // Both are not null, compare their values
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
        ParkItem park = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.park_item, parent, false);
        }

        // 파인드 뷰
        TextView parkOrder = convertView.findViewById(R.id.parkOrder);
        TextView parkName = convertView.findViewById(R.id.parkName);
        TextView parkType = convertView.findViewById(R.id.parkType);
        TextView parkDistance = convertView.findViewById(R.id.parkDistance);
        TextView parkPrice = convertView.findViewById(R.id.parkPrice);
        TextView parkConditionAndDiscount = convertView.findViewById(R.id.parkConditionAndDiscount);
        TextView parkPhone = convertView.findViewById(R.id.parkPhone);

        // 뷰 내용
        switch (park.getType()) {
            case 0 :
                parkType.setText("???");
                break;
            case 1 :
                parkType.setText("일반주차장");
                break;
            case 2 :
                parkType.setText("공영주차장");
                break;
            case 3 :
                parkType.setText("공유주차장");
                break;
            case 4 :
                parkType.setText("주소");
                break;
            case 5 :
                parkType.setText("장소");
                break;
            case 6 :
                parkType.setText("제보주차장");
                break;
            default :
                parkType.setText("뭐냐고");
        }

        parkOrder.setText (Integer.toString(position + 1));
        parkName.setText(park.getName());

        // 숫자 포맷 지정 (세 번째 자리에서 반올림)
        DecimalFormat formatter = new DecimalFormat("#.##");
        // 소수로 파싱 후, 포맷 적용하여 새로운 문자열 생성
        double number = Double.parseDouble(park.getRadius());
        String formattedDistanceString = formatter.format(number);
        parkDistance.setText(formattedDistanceString + "km");

        String parkPriceValue = park.getParkPrice();
        if (parkPriceValue != null && !parkPriceValue.equals("null") ) {
            if (Integer.parseInt(parkPriceValue) == 0) {
                parkPrice.setText("무료");
            }
            else {
                formatter = new DecimalFormat("#,###");
                number = Double.parseDouble(parkPriceValue);
                String formattedPriceString = formatter.format(number);
                if (park.getType() == 3) {
                    parkPrice.setText("시간 당 " + formattedPriceString + "pt");
                }
                else {
                    parkPrice.setText("시간 당 " + formattedPriceString + "원");
                }
            }
        }
        else {
            parkPrice.setText("");
        }

        if (park.getCondition() != null && park.getDiscount() != -1) {
            if (park.getDiscount() == 0) {
                parkConditionAndDiscount.setText(park.getCondition() + "/무료");
            }
            else {
                parkConditionAndDiscount.setText(park.getCondition() + "/" + park.getDiscount() + "원 할인");
            }
        }
        else {
            parkConditionAndDiscount.setText("");
        }

        String originalPhoneString = park.getPhone();
        String newPhoneString = "";
        if (originalPhoneString != null) {
            if (originalPhoneString.length() == 10) {
                newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 6) + "-" + originalPhoneString.substring(6);
            }
            else if (originalPhoneString.length() == 11) {
                newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 7) + "-" + originalPhoneString.substring(7);
            }
            else {
                newPhoneString = originalPhoneString;
            }
        }
        parkPhone.setText(newPhoneString);

        return convertView;
    }
}
