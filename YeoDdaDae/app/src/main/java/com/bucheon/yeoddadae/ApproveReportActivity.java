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

public class ApproveReportActivity extends AppCompatActivity {
    ReportDiscountParkAdapter rdpa;

    ImageButton approveReportBackBtn;
    ListView approveReportListView;
    TextView approveReportNoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_report);

        approveReportBackBtn = findViewById(R.id.approveReportBackBtn);
        approveReportListView = findViewById(R.id.approveReportListView);
        approveReportNoTxt = findViewById(R.id.approveReportNoTxt);

        approveReportBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        approveReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent approveReportInfoIntent = new Intent(getApplicationContext(), ApproveReportInformationActivity.class);
                approveReportInfoIntent.putExtra("documentId", ((ReportDiscountParkItem) rdpa.getItem(position)).getDocumentId());
                startActivity(approveReportInfoIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (rdpa != null) {
            rdpa.clearItem();
        }
        rdpa = new ReportDiscountParkAdapter(ApproveReportActivity.this);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadUnapprovedReports(new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> unapprovedReports = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneReport : unapprovedReports) {
                    String reporterId = (String) oneReport.get("reporterId");
                    String parkName = (String) oneReport.get("parkName");
                    String parkCondition = (String) oneReport.get("parkCondition");
                    long parkDiscount = (long) oneReport.get("parkDiscount");
                    long ratePerfectCount = (long) oneReport.get("ratePerfectCount");
                    long rateMistakeCount = (long) oneReport.get("rateMistakeCount");
                    long rateWrongCount = (long) oneReport.get("rateWrongCount");
                    boolean isCancelled = (boolean) oneReport.get("isCancelled");
                    boolean isApproval = (boolean) oneReport.get("isApproval");
                    Timestamp upTime = (Timestamp) oneReport.get("upTime");
                    String poiID = (String) oneReport.get("poiID");
                    String documentId = (String) oneReport.get("documentId");

                    rdpa.addItem(new ReportDiscountParkItem(reporterId, parkName, parkCondition, parkDiscount, ratePerfectCount, rateMistakeCount, rateWrongCount, isCancelled, isApproval, upTime, poiID, documentId));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        approveReportListView.setAdapter(rdpa);

                        if (rdpa.getCount() == 0) {
                            approveReportListView.setVisibility(View.GONE);
                            approveReportNoTxt.setVisibility(View.VISIBLE);
                        }
                        else {
                            approveReportListView.setVisibility(View.VISIBLE);
                            approveReportNoTxt.setVisibility(View.GONE);
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
