package com.bucheon.yeoddadae;

import androidx.annotation.Nullable;

public class ParkItem {
    private int type; // 1:일반, 2:공영, 3:공유, 4:주소, 5:장소, 6: 제보주차장
    private String name;
    private String radius;
    private String parkPrice;
    private String phone;
    private String condition;
    private long discount;
    private double lat;
    private double lon;
    private String poiId;
    private String firestoreDocumentId;

    public ParkItem(int type, String name, String radius, String parkPrice, String phone, String condition, long discount, String lat, String lon, String poiId, String firestoreDocumentId) {
        this.type = type;
        this.name = name;
        this.radius = radius;
        this.parkPrice = parkPrice;
        this.phone = phone;
        this.condition = condition;
        this.discount = discount;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        this.poiId = poiId;
        this.firestoreDocumentId = firestoreDocumentId;
    }

    public int getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public String getRadius() {
        return radius;
    }

    public String getParkPrice() {
        return parkPrice;
    }

    public String getPhone() {
        return phone;
    }

    public String getCondition() {
        return condition;
    }

    public long getDiscount() {
        return discount;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getFirebaseDocumentId() {
        return firestoreDocumentId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        ParkItem target = (ParkItem) obj;

        if (this.name.equals(target.getName()) && this.lat == target.getLat() && this.lon == target.getLon() && this.poiId.equals(target.getPoiId()) && this.firestoreDocumentId.equals(target.getFirebaseDocumentId())) {
            return true;
        }
        else {
            return false;
        }
    }
}
