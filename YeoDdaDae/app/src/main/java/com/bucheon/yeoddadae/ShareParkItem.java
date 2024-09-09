package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ShareParkItem {
    private String ownerId;
    private double lat;
    private double lon;
    private String parkDetailAddress;
    private boolean isApproval;
    private boolean isCancelled;
    private boolean isCalculated;
    private long price;
    private HashMap<String, ArrayList<String>> time;
    private Timestamp upTime;
    private String documentId;

    public ShareParkItem(String ownerId, double lat, double lon, String parkDetailAddress, boolean isApproval, boolean isCancelled, boolean isCalculated, long price, HashMap<String, ArrayList<String>> time, Timestamp upTime, String documentId) {
        this.ownerId = ownerId;
        this.lat = lat;
        this.lon = lon;
        this.parkDetailAddress = parkDetailAddress;
        this.isApproval = isApproval;
        this.isCancelled = isCancelled;
        this.isCalculated = isCalculated;
        this.price = price;
        this.time = time;
        this.upTime = upTime;
        this.documentId = documentId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getParkDetailAddress() {
        return parkDetailAddress;
    }

    public boolean isApproval() {
        return isApproval;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isCalculated() {
        return isCalculated;
    }

    public long getPrice() {
        return price;
    }

    public HashMap<String, ArrayList<String>> getTime() {
        return time;
    }

    public Timestamp getUpTime() {
        return upTime;
    }

    public String getDocumentId() {
        return documentId;
    }
}
