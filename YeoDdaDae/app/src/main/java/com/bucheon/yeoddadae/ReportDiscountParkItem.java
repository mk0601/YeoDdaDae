package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class ReportDiscountParkItem {
    String reporterId;
    String parkName;
    String condition;
    long discount;
    long ratePerfectCount;
    long rateMistakeCount;
    long rateWrongCount;
    boolean isCancelled;
    boolean isApproval;
    Timestamp upTime;
    String poiId;
    String documentId;

    public ReportDiscountParkItem(String reporterId, String parkName, String condition, long discount, long ratePerfectCount, long rateMistakeCount, long rateWrongCount, boolean isCancelled, boolean isApproval, Timestamp upTime, String poiId, String documentId) {
        this.reporterId = reporterId;
        this.parkName = parkName;
        this.condition = condition;
        this.discount = discount;
        this.ratePerfectCount = ratePerfectCount;
        this.rateMistakeCount = rateMistakeCount;
        this.rateWrongCount = rateWrongCount;
        this.isCancelled = isCancelled;
        this.isApproval = isApproval;
        this.upTime = upTime;
        this.poiId = poiId;
        this.documentId = documentId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getParkName() {
        return parkName;
    }

    public String getCondition() {
        return condition;
    }

    public long getDiscount() {
        return discount;
    }

    public long getRatePerfectCount() {
        return ratePerfectCount;
    }

    public long getRateMistakeCount() {
        return rateMistakeCount;
    }

    public long getRateWrongCount() {
        return rateWrongCount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isApproval() {
        return isApproval;
    }

    public Timestamp getUpTime() {
        return upTime;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getDocumentId() {
        return documentId;
    }
}
