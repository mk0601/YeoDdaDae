package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapGpsManager;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;
import java.util.HashMap;

public class AnotherReportDiscountParkActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    private final int PERMISSION_REQUEST_CODE = 1;
    String loginId;
    double nowLat;
    double nowLon;
    int km = -1;
    TMapGpsManager gpsManager;
    boolean firstInitCalled = false;
    ReportDiscountParkAdapter ra;

    ImageButton anotherReportBackBtn;
    Spinner anotherReportDistanceSpinner;
    ListView anotherReportListView;
    ImageButton toAddReportBtn;
    TextView anotherReportNoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_report_discount_park);

        anotherReportBackBtn = findViewById(R.id.anotherReportBackBtn);
        anotherReportDistanceSpinner = findViewById(R.id.anotherReportDistanceSpinner);
        anotherReportListView = findViewById(R.id.anotherReportListView);
        toAddReportBtn = findViewById(R.id.toAddReportBtn);
        anotherReportNoTxt = findViewById(R.id.anotherReportNoTxt);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.my_spinner_distance_items,
                R.layout.my_spinner
        );
        anotherReportDistanceSpinner.setAdapter(adapter);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (km != -1 && nowLat != 0 && nowLon != 0) {
            getReports(km, nowLat, nowLon);
        }

        checkPermission();
    }

    public void getReports (int distanceKm, double nowLat, double nowLon) {
        if (ra != null) {
            ra.clearItem();
        }
        ra = new ReportDiscountParkAdapter(AnotherReportDiscountParkActivity.this);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadAnotherReports(distanceKm, nowLat, nowLon, loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myReports = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneReport : myReports) {
                    String reporterId = (String) oneReport.get("reporterId");
                    String parkName = (String) oneReport.get("parkName");
                    String condition = (String) oneReport.get("parkCondition");
                    long discount = (long) oneReport.get("parkDiscount");
                    long ratePerfectCount = (long) oneReport.get("ratePerfectCount");
                    long rateMistakeCount = (long) oneReport.get("rateMistakeCount");
                    long rateWrongCount = (long) oneReport.get("rateWrongCount");
                    boolean isCancelled = (boolean) oneReport.get("isCancelled");
                    boolean isApproval = (boolean) oneReport.get("isApproval");
                    Timestamp upTime = (Timestamp) oneReport.get("upTime");
                    String poiID = (String) oneReport.get("poiID");
                    String documentId = (String) oneReport.get("documentId");

                    ra.addItem(new ReportDiscountParkItem(reporterId, parkName, condition, discount, ratePerfectCount, rateMistakeCount, rateWrongCount, isCancelled, isApproval, upTime, poiID, documentId));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        anotherReportListView.setAdapter(ra);

                        if (ra.getCount() == 0) {
                            anotherReportListView.setVisibility(View.GONE);
                            anotherReportNoTxt.setVisibility(View.VISIBLE);
                        }
                        else {
                            anotherReportListView.setVisibility(View.VISIBLE);
                            anotherReportNoTxt.setVisibility(View.GONE);
                        }
                    }
                });
            }
            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onLocationChange(Location location) {
        nowLat = location.getLatitude();
        Log.d(ContentValues.TAG, "onLocationChange 호출됨 : nowLat(경도) = " + nowLat);

        nowLon = location.getLongitude();
        Log.d(ContentValues.TAG, "onLocationChange 호출됨 : nowLon(위도) = " + nowLon);
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(ContentValues.TAG, "권한 있음");
            init();
        }
        else {
            Log.d(ContentValues.TAG, "권한 없음. 요청");
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
            Log.d(ContentValues.TAG, "권한요청에서 허가");
            init();
        }
        else {
            Toast.makeText(getApplicationContext(), "GPS 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            Log.d (ContentValues.TAG, "권한요청에서 거부or문제");
            finish();
        }
    }

    void init() {
        if (firstInitCalled == false) {
            Location currentLocation = SDKManager.getInstance().getCurrentPosition();
            nowLat = currentLocation.getLatitude();
            nowLon = currentLocation.getLongitude();

            // TMapGpsManager 설정
            gpsManager = new TMapGpsManager(this);
            gpsManager.setMinTime(500); // ms단위
            gpsManager.setMinDistance(1); // m단위
            gpsManager.setProvider(gpsManager.GPS_PROVIDER);
            gpsManager.OpenGps();
            // gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
            // gpsManager.OpenGps();
        }

        Log.d(TAG, "현재 Lat : "  + nowLat);
        Log.d(TAG, "현재 Lon : "  + nowLon);

        anotherReportBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toAddReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addReportIntent = new Intent(getApplicationContext(), AddReportDiscountParkActivity.class);
                addReportIntent.putExtra("loginId", loginId);
                startActivity(addReportIntent);
            }
        });

        anotherReportDistanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("무제한")) {
                    km = 0;
                    getReports(km, nowLat, nowLon);
                }
                else {
                    km = Integer.parseInt(selectedItem.substring(0, 1));
                    getReports(km, nowLat, nowLon);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        anotherReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toRateReportIntent = new Intent(getApplicationContext(), RateAnotherReportDiscountParkActivity.class);
                toRateReportIntent.putExtra("id", loginId);
                toRateReportIntent.putExtra("documentId", ((ReportDiscountParkItem) ra.getItem(position)).getDocumentId());
                startActivity(toRateReportIntent);
            }
        });

        firstInitCalled = true;
    }
}