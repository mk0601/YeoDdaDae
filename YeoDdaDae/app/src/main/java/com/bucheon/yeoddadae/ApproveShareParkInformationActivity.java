package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ApproveShareParkInformationActivity extends AppCompatActivity {
    String documentId;
    HashMap<String, Object> shareParkInfo;

    ImageButton approveShareParkInfoBackBtn;
    TextView approveShareParkInfoIdContentTxt;
    TextView approveShareParkInfoShareParkNewAddressContentTxt;
    TextView approveShareParkInfoShareParkOldAddressContentTxt;
    TextView approveShareParkInfoShareParkDetailaddressContentTxt;
    TextView approveShareParkInfoOwnerIdContentTxt;
    TextView approveShareParkInfoNameContentTxt;
    TextView approveShareParkInfoPhoneContentTxt;
    TextView approveShareParkInfoEmailContentTxt;
    TextView approveShareParkInfoRelationContentTxt;
    TextView approveShareParkInfoPriceContentTxt;
    TextView approveShareParkInfoHourPerTxt;
    TextView approveShareParkInfoPtTxt;
    TextView approveShareParkInfoShareTimeContentTxt;
    TextView approveShareParkInfoUpTimeContentTxt;
    ImageButton approveBtn;
    ImageButton rejectionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_share_park_information);

        approveShareParkInfoBackBtn = findViewById(R.id.approveShareParkInfoBackBtn);
        approveShareParkInfoIdContentTxt = findViewById(R.id.approveShareParkInfoIdContentTxt);
        approveShareParkInfoShareParkNewAddressContentTxt = findViewById(R.id.approveShareParkInfoShareParkNewAddressContentTxt);
        approveShareParkInfoShareParkOldAddressContentTxt = findViewById(R.id.approveShareParkInfoShareParkOldAddressContentTxt);
        approveShareParkInfoShareParkDetailaddressContentTxt = findViewById(R.id.approveShareParkInfoShareParkDetailaddressContentTxt);
        approveShareParkInfoOwnerIdContentTxt = findViewById(R.id.approveShareParkInfoOwnerIdContentTxt);
        approveShareParkInfoNameContentTxt = findViewById(R.id.approveShareParkInfoNameContentTxt);
        approveShareParkInfoPhoneContentTxt = findViewById(R.id.approveShareParkInfoPhoneContentTxt);
        approveShareParkInfoEmailContentTxt = findViewById(R.id.approveShareParkInfoEmailContentTxt);
        approveShareParkInfoRelationContentTxt = findViewById(R.id.approveShareParkInfoRelationContentTxt);
        approveShareParkInfoPriceContentTxt = findViewById(R.id.approveShareParkInfoPriceContentTxt);
        approveShareParkInfoHourPerTxt = findViewById(R.id.approveShareParkInfoHourPerTxt);
        approveShareParkInfoPtTxt = findViewById(R.id.approveShareParkInfoPtTxt);
        approveShareParkInfoShareTimeContentTxt = findViewById(R.id.approveShareParkInfoShareTimeContentTxt);
        approveShareParkInfoUpTimeContentTxt = findViewById(R.id.approveShareParkInfoUpTimeContentTxt);
        approveBtn = findViewById(R.id.approveBtn);
        rejectionBtn = findViewById(R.id.rejectionBtn);

        Intent inIntent = getIntent();
        documentId = inIntent.getStringExtra("documentId");

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadShareParkInfo(documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                shareParkInfo = (HashMap<String, Object>) data;
                init();
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        approveShareParkInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreDatabase fd = new FirestoreDatabase();
                fd.approveSharePark(documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "공유주차장 승인되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        rejectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApproveShareParkInformationActivity.this);
                builder.setTitle("거절 사유 입력");

                // Set up the input
                final EditText input = new EditText(ApproveShareParkInformationActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String cancelReason = replaceNewlinesAndTrim(input);
                        fd.cancelSharePark((String) shareParkInfo.get("ownerId"), documentId, cancelReason, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "공유주차장 거부되었습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(ApproveShareParkInformationActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(ApproveShareParkInformationActivity.this, R.color.sub));
            }
        });
    }

    void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                approveShareParkInfoIdContentTxt.setText(documentId);

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                    approveShareParkInfoShareParkNewAddressContentTxt.setText(adrresses[2]);
                                    approveShareParkInfoShareParkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });
                approveShareParkInfoShareParkDetailaddressContentTxt.setText((String) shareParkInfo.get("parkDetailAddress"));

                approveShareParkInfoOwnerIdContentTxt.setText((String) shareParkInfo.get("ownerId"));

                approveShareParkInfoNameContentTxt.setText((String) shareParkInfo.get("ownerName"));

                approveShareParkInfoPhoneContentTxt.setText((String) shareParkInfo.get("ownerPhone"));

                approveShareParkInfoEmailContentTxt.setText((String) shareParkInfo.get("ownerEmail"));

                approveShareParkInfoRelationContentTxt.setText((String) shareParkInfo.get("ownerParkingRelation"));
                
                if ((long) shareParkInfo.get("price") == 0) {
                    approveShareParkInfoHourPerTxt.setVisibility(View.GONE);
                    approveShareParkInfoPtTxt.setVisibility(View.GONE);
                    approveShareParkInfoPriceContentTxt.setText("무료");
                }
                else {
                    approveShareParkInfoHourPerTxt.setVisibility(View.VISIBLE);
                    approveShareParkInfoPtTxt.setVisibility(View.VISIBLE);
                    String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format((long) shareParkInfo.get("price"));

                    approveShareParkInfoPriceContentTxt.setText(formattedYdPoint);
                }

                HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) shareParkInfo.get("time");
                List<String> keys = new ArrayList<>(shareTime.keySet());
                Collections.sort(keys);
                StringBuilder textBuilder = new StringBuilder();
                for (String key : keys) {
                    ArrayList<String> values = shareTime.get(key);

                    String year = key.substring(0, 4);
                    String month = key.substring(4, 6);
                    String day = key.substring(6);

                    String startTimeString = values.get(0).substring(0,2) + ":" + values.get(0).substring(2);
                    String endTimeString = values.get(1).substring(0,2) + ":" + values.get(1).substring(2);

                    textBuilder.append(year + "년 " + month + "월 " + day + "일 " + startTimeString + "부터 " + endTimeString + "까지\n");
                }
                String shareTimeString = "";
                if (!textBuilder.toString().isEmpty()) {
                    shareTimeString = textBuilder.toString().substring(0, textBuilder.length() - 1);
                }
                approveShareParkInfoShareTimeContentTxt.setText(shareTimeString);

                Timestamp timestamp = (Timestamp) shareParkInfo.get("upTime");
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    approveShareParkInfoUpTimeContentTxt.setText(dateString);
                }
            }
        });
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
