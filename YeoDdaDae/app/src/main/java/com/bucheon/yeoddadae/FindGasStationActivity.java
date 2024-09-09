package com.bucheon.yeoddadae;


import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;

public class FindGasStationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback, TMapView.OnClickListenerCallback, SttService.SttCallback {
    private static final int PERMISSION_REQUEST_CODE = 1;
    boolean firstOnLocationChangeCalled = false; // onLocationChange가 처음 불림 여부
    boolean isItemSelected = false;
    boolean isLoadingFirstCalled = false;
    int recievedSort;
    int nowSort;
    AlertDialog loadingAlert;
    // 경복궁
    double lat = 37.578611; // 위도
    double lon = 126.977222; // 경도
    TMapPoint nowPoint = new TMapPoint(lat, lon);
    TMapPoint naviEndPoint;
    String naviEndPointName;
    TMapGpsManager gpsManager;
    TMapView tMapView;
    TMapCircle tMapCircle;
    TMapData tMapData;
    TMapMarkerItem tempMarker;
    GasStationItem tempGasStation;
    TMapPoint scrollSavedPoint;
    TMapMarkerItem selectedMarker;
    GasStationAdapter gasStationAdapter;

    Intent serviceIntent;
    SttService sttService;
    ServiceConnection serviceConnection;
    SttService.SttCallback sttCallback;
    SttDialog sd;

    ListView gasStationListView;
    ImageButton findGasStationBackBtn;
    ImageButton zoomOutBtn;
    ImageButton zoomInBtn;
    ImageButton gpsBtn;
    ImageButton findGasStationSttBtn;
    HorizontalScrollView gasStationSortHorizontalScrollView;
    ImageButton sortByDistanceBtn;
    ImageButton sortByGasolinePriceBtn;
    ImageButton sortByDieselPriceBtn;
    ImageButton sortByHighGasolinePriceBtn;
    ImageButton sortByHighDieselPriceBtn;
    ConstraintLayout naviConstLayout;
    ImageButton cancelNaviBtn;
    ImageButton toStartNaviBtn;

    Bitmap tmapMyLocationIcon;
    Bitmap tmapMarkerIcon;
    Bitmap tmapStartMarkerIcon;
    Bitmap tmapSelectedMarkerIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gas_station);

        // 뷰 정의
        gasStationListView = findViewById(R.id.gasStationListView);
        findGasStationBackBtn = findViewById(R.id.findGasStationBackBtn);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        findGasStationSttBtn = findViewById(R.id.findGasStationSttBtn);
        gasStationSortHorizontalScrollView = findViewById(R.id.gasStationSortHorizontalScrollView);
        sortByDistanceBtn = findViewById(R.id.sortByDistanceBtn);
        sortByGasolinePriceBtn = findViewById(R.id.sortByGasolinePriceBtn);
        sortByDieselPriceBtn = findViewById(R.id.sortByDieselPriceBtn);
        sortByHighGasolinePriceBtn = findViewById(R.id.sortByHighGasolinePriceBtn);
        sortByHighDieselPriceBtn = findViewById(R.id.sortByHighDieselPriceBtn);
        naviConstLayout = findViewById(R.id.naviConstLayout);
        cancelNaviBtn = findViewById(R.id.cancelNaviBtn);
        toStartNaviBtn = findViewById(R.id.toStartNaviBtn);

        // Bitmap 정의
        tmapMyLocationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location);
        tmapMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_marker);
        tmapStartMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_start_marker);
        tmapSelectedMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_marker);

        loadingStart(); // 로딩 시작

        // 메인액티비티에서 정렬기준 받기
        Intent inIntent = getIntent();
        recievedSort = inIntent.getIntExtra("SortBy", 0);
        if (recievedSort == 0) {
            Log.d(TAG, "sttSort가 0임 (오류)");
            finish();
        }

        initSttService();

        runOnUiThread(new Runnable() { // 로딩 완료까지 뷰 없애기
            @Override
            public void run() {
                gasStationListView.setVisibility(View.GONE);
                gasStationSortHorizontalScrollView.setVisibility(View.GONE);
            }
        });

        // 권한 확인 후 초기화
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

    private void init() { // 초기화
        // 최초 위치 한 번 받기
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        lat = currentLocation.getLatitude();
        lon = currentLocation.getLongitude();
        nowPoint = new TMapPoint(lat, lon);

        // TMapGpsManager 설정
        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(500); // ms단위
        gpsManager.setMinDistance(1); // m단위
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        // gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        // gpsManager.OpenGps();

        // TMapView 생성 및 보이기
        tMapView = new TMapView(this);
        LinearLayout linearLayoutTmap = findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView( tMapView );

        // TMapView 초기설정
        tMapView.setCenterPoint(lon, lat);
        tMapView.setLocationPoint(lon, lat);
        tMapView.setIcon(tmapMyLocationIcon);
        tMapView.setIconVisibility(true);

        // TMapCircle 초기설정
        tMapCircle = new TMapCircle();
        tMapCircle.setRadius(3000);
        tMapCircle.setCircleWidth(0);
        tMapCircle.setAreaColor(Color.rgb(0, 0, 0));
        tMapCircle.setAreaAlpha(25);
        tMapView.addTMapCircle("Circle", tMapCircle);

        findGasStation(recievedSort);

        // 버튼 클릭 이벤트 리스너들
        findGasStationBackBtn.setOnClickListener(new View.OnClickListener() { // 액티비티 종료
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() { // GPS 현재 위치로 맵 중심점 이동
            @Override
            public void onClick(View v) {
                tMapView.setCenterPoint(lon, lat);
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

        findGasStationSttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sttService.startListeningForMainCommand();
            }
        });

        sortByDistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(1);
            }
        });

        sortByGasolinePriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(2);
            }
        });

        sortByDieselPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(3);
            }
        });

        sortByHighGasolinePriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(4);
            }
        });

        sortByHighDieselPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(5);
            }
        });

        gasStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isItemSelected) { // 클릭한 GasStationItem을 가져옴
                    GasStationItem targetGasStation = (GasStationItem) parent.getItemAtPosition(position);
                    TMapMarkerItem targetMarker = tMapView.getMarkerItemFromID(targetGasStation.getName());

                    if (targetGasStation != null && targetMarker != null) {
                        gasStationSelect(targetMarker, targetGasStation);
                    }
                }
            }
        });

        cancelNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isItemSelected = false;
                        tMapView.removeTMapPath();

                        naviConstLayout.setVisibility(View.GONE);

                        findGasStation(nowSort);
                    }
                });
            }
        });

        toStartNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TMapTapi tt = new TMapTapi(FindGasStationActivity.this);
                boolean isTmapApp = tt.isTmapApplicationInstalled();
                if (isTmapApp) {
                    tt.invokeRoute(naviEndPointName, (float) naviEndPoint.getLongitude(), (float) naviEndPoint.getLatitude());
                }
                else {
                    Toast.makeText(getApplicationContext(), "TMAP이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 뷰 보이기
        gasStationListView.setVisibility(View.VISIBLE);
        gasStationSortHorizontalScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) { // TMapMarkerItem이 클릭됬다면
            tempMarker = arrayList.get(0); // endMarker = 클릭한 TMapMarkerItem
            tempGasStation = gasStationAdapter.findItem(tempMarker.getName());
            scrollSavedPoint = tMapView.getCenterPoint();
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) {
            TMapMarkerItem targetMarker = arrayList.get(0); // endMarker = 클릭한 TMapMarkerItem
            GasStationItem targetGasStation = gasStationAdapter.findItem(targetMarker.getName());

            if (scrollSavedPoint.equals(tMapView.getCenterPoint()) && targetMarker == tempMarker && targetGasStation == tempGasStation && targetGasStation != null && targetMarker != null) {
                gasStationSelect(targetMarker, targetGasStation);
            }
        }

        tempMarker = null;
        tempGasStation = null;
        scrollSavedPoint = null;

        return false;
    }

    void gasStationSelect(TMapMarkerItem targetMarker, GasStationItem targetGasStation) {
        // gasStationListView에 targetGasStation만 있도록
        GasStationAdapter tempGasStationAdapter = new GasStationAdapter();
        tempGasStationAdapter.addItem(targetGasStation);
        gasStationListView.setAdapter(tempGasStationAdapter);

        // 도착점 설정
        naviEndPoint = new TMapPoint(targetGasStation.getLat(), targetGasStation.getLon());
        naviEndPointName = targetGasStation.getName();

        // 길 찾기 및 선 표시
        TMapData tmapdata = new TMapData();
        tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                if (selectedMarker != null) {
                    selectedMarker.setIcon(tmapMarkerIcon);
                }

                tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                targetMarker.setIcon(tmapSelectedMarkerIcon);

                selectedMarker = targetMarker;

                tMapView.addTMapPath(polyLine);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tMapView.setCenterPoint(selectedMarker.longitude, selectedMarker.latitude);

                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                        gasStationListView.getLayoutParams().height = (int) px;
                        gasStationListView.requestLayout();

                        TextView gasStationOrder = findViewById(R.id.gasStationOrder);
                        gasStationOrder.setVisibility(View.INVISIBLE);

                        TextView gasStationName = findViewById(R.id.gasStationName);
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) gasStationName.getLayoutParams();
                        params.leftToLeft = R.id.gasStationItemConstraintLayout;
                        gasStationName.setLayoutParams(params);

                        gasStationSortHorizontalScrollView.setVisibility(View.GONE);
                        naviConstLayout.setVisibility(View.VISIBLE);

                        isItemSelected = true;
                    }
                });
            }
        });
    }

    void findGasStation(int sortBy) { // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:휘발유가순, 4: 경유가순 5: 고급휘발유가순, 6: 고급경유가순
        Log.d (TAG, "findGasStation 시작");

        loadingStart();

        tMapData = new TMapData();
        tMapData.findAroundNamePOI(nowPoint, "주유소", 3, 200, new TMapData.FindAroundNamePOIListenerCallback () {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                if (arrayList == null) {
                    loadingStop();
                    return;
                }
                tMapView.removeAllTMapCircle();
                tMapView.removeAllMarkerItem();
                gasStationAdapter = new GasStationAdapter();

                for (int i = 0; i < arrayList.size(); i++) {
                    TMapPOIItem item = arrayList.get(i);

                    gasStationAdapter.addItem(new GasStationItem(item.name, item.radius, item.hhPrice, item.ggPrice, item.highHhPrice, item.highGgPrice, item.telNo, item.stId, item.frontLat, item.frontLon));
                    TMapPoint tpoint = new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon));
                    TMapMarkerItem tItem = new TMapMarkerItem();
                    tItem.setTMapPoint(tpoint);
                    tItem.setName(item.name);
                    tItem.setVisible(TMapMarkerItem.VISIBLE);
                    tItem.setIcon(tmapMarkerIcon);
                    tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                    tMapView.addMarkerItem(item.name, tItem);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gasStationListView.setAdapter(gasStationAdapter);

                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        float px;

                        switch (gasStationAdapter.getSize()) {
                            case 0:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, displayMetrics);
                                break;
                            case 1:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                break;
                            case 2:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, displayMetrics);
                                break;
                            default:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, displayMetrics);
                        }

                        gasStationListView.getLayoutParams().height = (int) px;
                        gasStationListView.requestLayout();

                        switch (sortBy) {
                            case 1 :
                                sortByDistanceBtn.setImageResource(R.drawable.gradate_button);
                                sortByGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                gasStationAdapter.sortByDistance();
                                break;
                            case 2 :
                                sortByDistanceBtn.setImageResource(R.drawable.disabled_button);
                                sortByGasolinePriceBtn.setImageResource(R.drawable.gradate_button);
                                sortByDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                gasStationAdapter.sortByGasolinePrice();
                                break;
                            case 3 :
                                sortByDistanceBtn.setImageResource(R.drawable.disabled_button);
                                sortByGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByDieselPriceBtn.setImageResource(R.drawable.gradate_button);
                                sortByHighGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                gasStationAdapter.sortByDieselPrice();
                                break;
                            case 4 :
                                sortByDistanceBtn.setImageResource(R.drawable.disabled_button);
                                sortByGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighGasolinePriceBtn.setImageResource(R.drawable.gradate_button);
                                sortByHighDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                gasStationAdapter.sortByHighGasolinePrice();
                                break;
                            case 5 :
                                sortByDistanceBtn.setImageResource(R.drawable.disabled_button);
                                sortByGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByDieselPriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighGasolinePriceBtn.setImageResource(R.drawable.disabled_button);
                                sortByHighDieselPriceBtn.setImageResource(R.drawable.gradate_button);
                                gasStationAdapter.sortByHighDieselPrice();
                                break;
                        }

                        gasStationListView.setVisibility(View.VISIBLE);
                        gasStationSortHorizontalScrollView.setVisibility(View.VISIBLE);
                        naviConstLayout.setVisibility(View.GONE);

                        isItemSelected = false;
                    }
                });
                tMapCircle.setCenterPoint( nowPoint );
                tMapView.addTMapCircle("Circle", tMapCircle);

                nowSort = sortBy;

                loadingStop();
            }
        });
    }

    @Override
    public void onLocationChange(Location location){
        lat = location.getLatitude();
        Log.d(TAG, "onLocationChange 호출됨 : lat(경도) = " + lat);

        lon = location.getLongitude();
        Log.d(TAG, "onLocationChange 호출됨 : lon(위도) = " + lon);

        nowPoint = gpsManager.getLocation();
        tMapView.setLocationPoint(lon, lat);

        if (!firstOnLocationChangeCalled) {
            tMapView.setCenterPoint(lon, lat);
            findGasStation(recievedSort);
            firstOnLocationChangeCalled = true;
        }
    }

    public void loadingStart() {
        Log.d(TAG, "로딩 시작");

        if (!isLoadingFirstCalled) { // 로딩 AlertDialog 지정
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("로딩 중").setCancelable(false).setNegativeButton("액티비티 종료", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            loadingAlert = builder.create();
            isLoadingFirstCalled = true;
        }

        if (loadingAlert != null && loadingAlert.isShowing()) {
            return; // 이미 보여지고 있다면 함수 종료
        }

        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingAlert.show();
                    loadingAlert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(FindGasStationActivity.this, R.color.sub));
                    loadingAlert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(FindGasStationActivity.this, R.color.sub));
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }
    }

    public void loadingStop() {
        Log.d(TAG, "로딩 끝");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingAlert != null && loadingAlert.isShowing()) {
                    loadingAlert.dismiss();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    void initSttService() {
        sd = new SttDialog(this, new SttDialogListener() {
            @Override
            public void onMessageSend(String message) {
                if (message.equals("SttDialog버튼클릭")) {
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                    sttService.startListeningForMainCommand();
                }
                else if (message.equals("SttDialog닫힘")) {
                    sttService.startListeningForWakeUpWord();
                }
            }
        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SttService.SttBinder binder = (SttService.SttBinder) service;
                sttService = binder.getService();
                sttService.setSttCallback(sttCallback);
                Log.d(TAG, "FindGasStationActivity : STT 서비스 연결됨");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "FindGasStationActivity : STT 서비스 연결 해제됨");
            }
        };

        serviceIntent = new Intent(this, SttService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        sttCallback = new SttService.SttCallback() {
            @Override
            public void onMainCommandReceived(String mainCommand) {
                FindGasStationActivity.this.onMainCommandReceived(mainCommand);
            }

            @Override
            public void onUpdateUI(String message) {
                FindGasStationActivity.this.onUpdateUI(message);
            }
        };
    }

    @Override
    public void onMainCommandReceived(String mainCommand) {
        Log.d(TAG, "FindGasStationActivity에서 받은 명령: " + mainCommand);
        // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:휘발유가순, 4: 경유가순 5: 고급휘발유가순, 6: 고급경유가순
        if (mainCommand.contains("메인")) {
            sd.dismiss();
            finish();
        }
        else if (mainCommand.contains("돌아") || mainCommand.contains("이전")) {
            if (isItemSelected) {
                sd.dismiss();
                cancelNaviBtn.callOnClick();
            }
            else {
                sd.dismiss();
                finish();
            }
        }
        else if (!isItemSelected && (mainCommand.contains("순") || mainCommand.contains("정렬") || mainCommand.contains("찾"))) {
            sd.dismiss();
            if (mainCommand.contains("고급") && (mainCommand.contains("휘발") || mainCommand.contains("가솔린"))) {
                findGasStation(4);
            }
            else if (mainCommand.contains("고급") && (mainCommand.contains("경유") || mainCommand.contains("디젤"))) {
                findGasStation(5);
            }
            else if (mainCommand.contains("휘발") || mainCommand.contains("가솔린")) {
                findGasStation(2);
            }
            else if (mainCommand.contains("경유") || mainCommand.contains("디젤")) {
                findGasStation(3);
            }
            else {
                findGasStation(1);
            }
        }
        else if (isItemSelected && (mainCommand.contains("안내") || mainCommand.contains("내비") || mainCommand.contains("시작") || mainCommand.contains("출발"))) {
            sd.dismiss();
            TMapTapi tt = new TMapTapi(FindGasStationActivity.this);
            boolean isTmapApp = tt.isTmapApplicationInstalled();
            if (isTmapApp) {
                tt.invokeRoute(naviEndPointName, (float) naviEndPoint.getLongitude(), (float) naviEndPoint.getLatitude());
            }
            else {
                Toast.makeText(getApplicationContext(), "TMAP이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            int number = 0;

            if (mainCommand.matches(".*\\d.*")) {
                String numberStr = mainCommand.replaceAll("\\D", ""); // 숫자 이외의 문자 제거
                number = Integer.parseInt(numberStr);
            }
            else if (mainCommand.matches(".*[일이삼사오육칠팔구십].*")){
                String numberStr = mainCommand.replaceAll("[^일이삼사오육칠팔구십]", ""); // 한글 숫자 이외의 문자 제거
                switch (numberStr) {
                    case "일":
                        number = 1;
                        break;
                    case "이":
                        number = 2;
                        break;
                    case "삼":
                        number = 3;
                        break;
                    case "사":
                        number = 4;
                        break;
                    case "오":
                        number = 5;
                        break;
                    case "육":
                        number = 6;
                        break;
                    case "칠":
                        number = 7;
                        break;
                    case "팔":
                        number = 8;
                        break;
                    case "구":
                        number = 9;
                        break;
                    case "십":
                        number = 10;
                        break;
                }
            }
            Log.d (TAG, "number 는 " + number);
            if (number != 0) {
                if (gasStationAdapter != null &&  0 < gasStationAdapter.getSize() && 0 < number && number <= gasStationAdapter.getSize()) {
                    sd.dismiss();

                    GasStationItem targetGasStation = (GasStationItem) gasStationAdapter.getItem(number - 1);
                    TMapMarkerItem targetMarker = tMapView.getMarkerItemFromID(targetGasStation.getName());

                    if (targetGasStation != null && targetMarker != null) {
                        gasStationSelect(targetMarker, targetGasStation);
                    }
                }
                else {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt(number + "번째 결과를 찾을 수 없습니다");
                }
            }
            else {
                sd.changeToInactivateIcon();
                sd.setSttStatusTxt(mainCommand + "\n알 수 없는 명령어입니다");
            }
        }
    }

    @Override
    public void onUpdateUI(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.equals("메인명령어듣는중")) {
                    if (!sd.isShowing()) {
                        sd.show();
                    }
                    sd.changeToActiveIcon();
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                }
                else if (message.equals("음성인식실패")) {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt("음성 인식 실패");
                }
                else if (message.equals("타임아웃")) {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt("음성 인식 타임 아웃");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loadingAlert.isShowing()) {
            loadingAlert.dismiss();
        }
        if (sd.isShowing()) {
            sd.dismiss();
        }

        if (serviceConnection != null && serviceIntent != null) {
            unbindService(serviceConnection);
            stopService(serviceIntent);
            serviceConnection = null;
            serviceIntent = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        initSttService();
    }
}
