package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.address_info.TMapAddressInfo;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;

public class GpsCertificationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private final int PERMISSION_REQUEST_CODE = 1;
    boolean isCloseEnough = false;
    boolean firstOnLocationChangeCalled = false;
    // 경복궁
    Double nowLat = 37.578611;
    Double nowLon= 126.977222;
    TMapGpsManager gpsManager;
    TMapView tMapView;
    TMapCircle tMapCircle;

    LinearLayout linearLayoutTmap;
    ImageButton decisionBtn;
    TextView decisionTxt;
    ConstraintLayout addressLayout;
    TextView newAddressTxt;
    TextView oldAddressTxt;
    ImageButton zoomOutBtn;
    ImageButton zoomInBtn;
    ImageButton gpsBtn;
    ImageButton gpsCerificationBackBtn;

    Bitmap tmapMyLocationIcon;
    Bitmap tmapMarkerIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_cerification);

        linearLayoutTmap = findViewById(R.id.linearLayoutTmap);
        decisionBtn = findViewById(R.id.decisionBtn);
        decisionTxt = findViewById(R.id.decisionTxt);
        addressLayout = findViewById(R.id.addressLayout);
        newAddressTxt = findViewById(R.id.newAddressTxt);
        oldAddressTxt = findViewById(R.id.oldAddressTxt);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        gpsCerificationBackBtn = findViewById(R.id.gpsCerificationBackBtn);

        tmapMyLocationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location);
        tmapMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_marker);

        checkPermission();
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한 있음");
            init();
        }
        else {
            Log.d(TAG, "권한 없음. 요청");
            String[] permissionArr = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionArr, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한요청에서 허가");
            init();
        }
        else {
            Toast.makeText(getApplicationContext(), "GPS 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            Log.d (TAG, "권한요청에서 거부or문제");
            finish();
        }
    }

    private void init() {
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        nowLat = currentLocation.getLatitude();
        nowLon = currentLocation.getLongitude();

        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(500); // ms단위
        gpsManager.setMinDistance(1); // m단위
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        // gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        // gpsManager.OpenGps();

        tMapView = new TMapView(this);
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView( tMapView );

        tMapView.setCenterPoint(nowLon, nowLat);
        tMapView.setLocationPoint(nowLon, nowLat);
        tMapView.setIcon(tmapMyLocationIcon);
        tMapView.setIconVisibility(true);

        tMapCircle = new TMapCircle();
        tMapCircle.setCenterPoint(new TMapPoint(nowLat, nowLon));
        tMapCircle.setRadius(200);
        tMapCircle.setCircleWidth(0);
        tMapCircle.setAreaColor(Color.rgb(0, 0, 0));
        tMapCircle.setAreaAlpha(25);
        tMapView.addTMapCircle("Circle", tMapCircle);

        gpsBtn.setOnClickListener(new View.OnClickListener() { // GPS 현재 위치로 맵 중심점 이동
            @Override
            public void onClick(View v) {
                tMapView.setCenterPoint(nowLon, nowLat);
                tMapView.setZoomLevel(16);
            }
        });

        zoomOutBtn.setOnClickListener(new View.OnClickListener() { // 맵 축소
            @Override
            public void onClick(View v) {
                if (tMapView.getZoomLevel() >= 8 ) {
                    tMapView.MapZoomOut();
                    Log.d(TAG, "축소, 현재 ZoomLevel : " + tMapView.getZoomLevel());
                }
            }
        });

        zoomInBtn.setOnClickListener(new View.OnClickListener() { // 맵 확대
            @Override
            public void onClick(View v) {
                if (tMapView.getZoomLevel() <= 17 ) {
                    tMapView.MapZoomIn();
                    Log.d(TAG, "확대, 현재 ZoomLevel : " + tMapView.getZoomLevel());
                }
            }
        });

        gpsCerificationBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        decisionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCloseEnough) {
                    // 성공적으로 작업이 완료되었을 때
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", tMapView.getMarkerItemFromID("temp").latitude);
                    resultIntent.putExtra("lon", tMapView.getMarkerItemFromID("temp").longitude);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                tMapView.removeAllMarkerItem();

                TMapMarkerItem tItem = new TMapMarkerItem();
                tItem.setTMapPoint(tMapPoint);
                tItem.setName("temp");
                tItem.setVisible(TMapMarkerItem.VISIBLE);
                tItem.setIcon(tmapMarkerIcon);
                tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                tMapView.addMarkerItem("temp", tItem);

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding(tItem.latitude, tItem.longitude, "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");

                                    newAddressTxt.setText(adrresses[2]);
                                    oldAddressTxt.setText(adrresses[1]);
                                    addressLayout.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });

                checkDistance();

                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });
    }

    public void checkDistance() {
        if (tMapView.getMarkerItemFromID("temp") != null && firstOnLocationChangeCalled) {
            TMapPolyLine tpolyline = new TMapPolyLine();
            tpolyline.addLinePoint(new TMapPoint(nowLat, nowLon));
            tpolyline.addLinePoint(tMapView.getMarkerItemFromID("temp").getTMapPoint());

            if (tpolyline.getDistance() < 200) {
                isCloseEnough = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        decisionBtn.setVisibility(View.VISIBLE);
                        decisionTxt.setVisibility(View.VISIBLE);
                    }
                });
            }
            else {
                isCloseEnough = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        decisionBtn.setVisibility(View.GONE);
                        decisionTxt.setVisibility(View.GONE);
                    }
                });
            }
        }
    }


    @Override
    public void onLocationChange(Location location) {
        nowLat = location.getLatitude();
        Log.d(TAG, "onLocationChange 호출됨 : lat(경도) = " + nowLat);

        nowLon = location.getLongitude();
        Log.d(TAG, "onLocationChange 호출됨 : lon(위도) = " + nowLon);

        tMapView.removeAllTMapCircle();
        tMapView.setLocationPoint(nowLon, nowLat);
        tMapCircle.setCenterPoint(new TMapPoint(nowLat, nowLon));
        tMapView.addTMapCircle("Circle", tMapCircle);

        checkDistance();

        if (!firstOnLocationChangeCalled) {
            tMapView.setCenterPoint(nowLon, nowLat);
            firstOnLocationChangeCalled = true;
        }
    }
}
