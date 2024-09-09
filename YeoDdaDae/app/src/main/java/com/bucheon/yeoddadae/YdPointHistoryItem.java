package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class YdPointHistoryItem {
    String type;
    long point;
    String refundBank;
    String refundAccountNumber;
    String spendType;
    String reservationId;
    String receiveType;
    Timestamp upTime;
    String documentId;

    public YdPointHistoryItem(String type, long point, String refundBank, String refundAccountNumber, String spendType, String reservationId, String receiveType, Timestamp upTime, String documentId) {
        this.type = type;
        this.point = point;
        this.refundBank = refundBank;
        this.refundAccountNumber = refundAccountNumber;
        this.spendType = spendType;
        this.reservationId = reservationId;
        this.receiveType = receiveType;
        this.upTime = upTime;
        this.documentId = documentId;
    }

    public String getType() {
        return type;
    }

    public long getPoint() {
        return point;
    }

    public String getRefundBank() {
        return refundBank;
    }

    public String getRefundAccountNumber() {
        return refundAccountNumber;
    }

    public String getSpendType() {
        return spendType;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getReceiveType() {
        return receiveType;
    }

    public Timestamp getUpTime() {
        return upTime;
    }

    public String getDocumentId() {
        return documentId;
    }
}
