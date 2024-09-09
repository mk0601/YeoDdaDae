package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GasStationAdapter extends BaseAdapter {
    ArrayList<GasStationItem> items = new ArrayList<GasStationItem>();

    public void addItem(GasStationItem item) {
        items.add(item);
    }

    public GasStationItem findItem(String gasStationItemName) {
        for (GasStationItem item : items) {
            if (item.getName().equals(gasStationItemName)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public int getSize() {
        return items.size();
    }

    public void sortByDistance () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<GasStationItem>() {
                @Override
                public int compare(GasStationItem o1, GasStationItem o2) {
                    // Compare by star rate in descending order
                    return Double.compare(Double.parseDouble(o1.getRadius()), Double.parseDouble(o2.getRadius()));
                }
            });
            notifyDataSetChanged();
        }
    }

    public void sortByGasolinePrice () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<GasStationItem>() {
                @Override
                public int compare(GasStationItem o1, GasStationItem o2) {
                    if (o1.getGasolinePrice().equals("0") && !o2.getGasolinePrice().equals("0")) {
                        return 1;
                    } else if (!o1.getGasolinePrice().equals("0") && o2.getGasolinePrice().equals("0")) {
                        return -1;
                    } else {
                        return Long.compare(Long.parseLong(o1.getGasolinePrice()), Long.parseLong(o2.getGasolinePrice()));
                    }
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public void sortByDieselPrice () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<GasStationItem>() {
                @Override
                public int compare(GasStationItem o1, GasStationItem o2) {
                    // Compare by diesel price in ascending order
                    if (o1.getDieselPrice().equals("0") && !o2.getDieselPrice().equals("0")) {
                        // o1의 가격이 "0"인 경우, o2를 더 우선시한다.
                        return 1;
                    } else if (!o1.getDieselPrice().equals("0") && o2.getDieselPrice().equals("0")) {
                        // o2의 가격이 "0"인 경우, o1을 더 우선시한다.
                        return -1;
                    } else {
                        // 둘 다 "0"이거나 둘 다 "0"이 아닌 경우, 일반적인 비교 수행
                        return Long.compare(Long.parseLong(o1.getDieselPrice()), Long.parseLong(o2.getDieselPrice()));
                    }
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public void sortByHighGasolinePrice() {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<GasStationItem>() {
                @Override
                public int compare(GasStationItem o1, GasStationItem o2) {
                    // Compare by diesel price in ascending order
                    if (o1.getHighGasolinePrice().equals("0") && !o2.getHighGasolinePrice().equals("0")) {
                        // o1의 가격이 "0"인 경우, o2를 더 우선시한다.
                        return 1;
                    } else if (!o1.getHighGasolinePrice().equals("0") && o2.getHighGasolinePrice().equals("0")) {
                        // o2의 가격이 "0"인 경우, o1을 더 우선시한다.
                        return -1;
                    } else {
                        // 둘 다 "0"이거나 둘 다 "0"이 아닌 경우, 일반적인 비교 수행
                        return Long.compare(Long.parseLong(o1.getHighGasolinePrice()), Long.parseLong(o2.getHighGasolinePrice()));
                    }
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public void sortByHighDieselPrice() {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<GasStationItem>() {
                @Override
                public int compare(GasStationItem o1, GasStationItem o2) {
                    // Compare by diesel price in ascending order
                    if (o1.getHighDieselPrice().equals("0") && !o2.getHighDieselPrice().equals("0")) {
                        // o1의 가격이 "0"인 경우, o2를 더 우선시한다.
                        return 1;
                    } else if (!o1.getHighDieselPrice().equals("0") && o2.getHighDieselPrice().equals("0")) {
                        // o2의 가격이 "0"인 경우, o1을 더 우선시한다.
                        return -1;
                    } else {
                        // 둘 다 "0"이거나 둘 다 "0"이 아닌 경우, 일반적인 비교 수행
                        return Long.compare(Long.parseLong(o1.getHighDieselPrice()), Long.parseLong(o2.getHighDieselPrice()));
                    }
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
        GasStationItem gasStation = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gas_station_item, parent, false);
        }
        
        // 파인드 뷰
        TextView gasStationOrder = convertView.findViewById(R.id.gasStationOrder);
        TextView gasStationName = convertView.findViewById(R.id.gasStationName);
        TextView gasStationDistance = convertView.findViewById(R.id.gasStationDistance);
        TextView gasStationBrand = convertView.findViewById(R.id.gasStationBrand);
        TextView gasStationOilPrice = convertView.findViewById(R.id.gasStationOilPrice);
        TextView gasStationPhone = convertView.findViewById(R.id.gasStationPhone);

        // 뷰 내용
        gasStationOrder.setText (Integer.toString(position + 1));
        gasStationName.setText(gasStation.getName());

        // 숫자 포맷 지정 (세 번째 자리에서 반올림)
        DecimalFormat formatter = new DecimalFormat("#.##");
        // 소수로 파싱 후, 포맷 적용하여 새로운 문자열 생성
        double number = Double.parseDouble(gasStation.getRadius());
        String formattedDistanceString = formatter.format(number);
        gasStationDistance.setText(formattedDistanceString + "km");

        gasStationBrand.setText(gasStation.getBrand());

        String totalOilPriceString = "";
        // 숫자 포맷 지정
        formatter = new DecimalFormat("#,###");
        // 숫자 문자열을 long 타입으로 파싱 후, 포맷 적용하여 새로운 문자열 생성
        number = Long.parseLong(gasStation.getGasolinePrice());
        String formattedGasolinePriceString = formatter.format(number);
        number = Long.parseLong(gasStation.getDieselPrice());
        String formattedDieselPriceString = formatter.format(number);
        totalOilPriceString += "휘 " + formattedGasolinePriceString + "/경 " + formattedDieselPriceString;

        if (!gasStation.getHighGasolinePrice().equals("0") && !gasStation.getHighDieselPrice().equals("0")) {
            number = Long.parseLong(gasStation.getHighGasolinePrice());
            String formattedHighGasolinePriceString = formatter.format(number);
            number = Long.parseLong(gasStation.getHighDieselPrice());
            String formattedHighDieselPriceString = formatter.format(number);

            totalOilPriceString += "\n고급휘 " + formattedHighGasolinePriceString + "/고급경 " + formattedHighDieselPriceString;
        }
        else if (!gasStation.getHighGasolinePrice().equals("0")) {
            number = Long.parseLong(gasStation.getHighGasolinePrice());
            String formattedHighGasolinePriceString = formatter.format(number);
            totalOilPriceString += "\n고급휘 " + formattedHighGasolinePriceString;
        }
        else if (!gasStation.getHighDieselPrice().equals("0")) {
            number = Long.parseLong(gasStation.getHighDieselPrice());
            String formattedHighDieselPriceString = formatter.format(number);
            totalOilPriceString += "\n고급경 " + formattedHighDieselPriceString;
        }
        gasStationOilPrice.setText(totalOilPriceString);

        String originalPhoneString = gasStation.getPhone();
        String newPhoneString = "";
        if (originalPhoneString.length() == 10) {
            newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 6) + "-" + originalPhoneString.substring(6);
        }
        else if (originalPhoneString.length() == 11) {
            newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 7) + "-" + originalPhoneString.substring(7);
        }
        else {
            newPhoneString = originalPhoneString;
        }
        gasStationPhone.setText(newPhoneString);

        return convertView;
    }
}
