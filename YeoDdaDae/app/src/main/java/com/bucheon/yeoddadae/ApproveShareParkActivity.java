package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ApproveShareParkActivity extends AppCompatActivity {
    ShareParkAdapter spa;

    ImageButton approveShareParkBackBtn;
    ListView approveShareParkListView;
    TextView approveShareParkNoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_share_park);

        approveShareParkBackBtn = findViewById(R.id.approveShareParkBackBtn);
        approveShareParkListView = findViewById(R.id.approveShareParkListView);
        approveShareParkNoTxt = findViewById(R.id.approveShareParkNoTxt);

        approveShareParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        approveShareParkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent approveShareParkInfoIntent = new Intent(getApplicationContext(), ApproveShareParkInformationActivity.class);
                approveShareParkInfoIntent.putExtra("documentId", ((ShareParkItem) spa.getItem(position)).getDocumentId());
                startActivity(approveShareParkInfoIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (spa != null) {
            spa.clearItem();
        }
        spa = new ShareParkAdapter(ApproveShareParkActivity.this);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadUnapprovedShareParks(new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myShareParks = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneSharePark : myShareParks) {
                    String ownerId = (String) oneSharePark.get("ownerId");
                    double lat = (double) oneSharePark.get("lat");
                    double lon = (double) oneSharePark.get("lon");
                    String parkDetailAddress = (String) oneSharePark.get("parkDetailAddress");
                    boolean isApproval = (boolean) oneSharePark.get("isApproval");
                    boolean isCancelled = (boolean) oneSharePark.get("isCancelled");
                    boolean isCalculated = (boolean) oneSharePark.get("isCalculated");
                    long price = (long) oneSharePark.get("price");
                    HashMap<String, ArrayList<String>> time = (HashMap<String, ArrayList<String>>) oneSharePark.get("time");
                    Timestamp upTime = (Timestamp) oneSharePark.get("upTime");
                    String documentId = (String) oneSharePark.get("documentId");

                    spa.addItem(new ShareParkItem(ownerId, lat, lon, parkDetailAddress, isApproval, isCancelled, isCalculated, price, time, upTime, documentId));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        approveShareParkListView.setAdapter(spa);

                        if (spa.getCount() == 0) {
                            approveShareParkListView.setVisibility(View.GONE);
                            approveShareParkNoTxt.setVisibility(View.VISIBLE);
                        }
                        else {
                            approveShareParkListView.setVisibility(View.VISIBLE);
                            approveShareParkNoTxt.setVisibility(View.GONE);
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
}
