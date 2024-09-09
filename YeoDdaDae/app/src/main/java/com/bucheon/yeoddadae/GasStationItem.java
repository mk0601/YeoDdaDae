package com.bucheon.yeoddadae;

public class GasStationItem {
    private String name;
    private String radius;
    private String gasolinePrice; // 휘발유가
    private String dieselPrice; // 경유가
    private String highGasolinePrice;
    private String highDieselPrice;
    private String phone;
    private String brand;
    private double lat;
    private double lon;

    public GasStationItem(String name, String radius, String gasolinePrice, String dieselPrice, String highGasolinePrice, String highDieselPrice, String phone, String brand, String lat, String lon) {
        this.name = name;
        this.radius = radius;
        this.gasolinePrice = gasolinePrice;
        this.dieselPrice = dieselPrice;
        this.highGasolinePrice = highGasolinePrice;
        this.highDieselPrice = highDieselPrice;
        this.phone = phone;
        this.brand = brand;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    public String getName() {
        return name;
    }

    public String getRadius() {
        return radius;
    }

    public String getGasolinePrice() {
        return gasolinePrice;
    }

    public String getDieselPrice() {
        return dieselPrice;
    }
    public String getHighGasolinePrice() {return highGasolinePrice;}

    public String getHighDieselPrice() {return highDieselPrice;}

    public String getPhone() {
        return phone;
    }

    public String getBrand() { return brand; }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
