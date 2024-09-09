package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReportDiscountParkFragment extends Fragment {
    String loginId;
    ReportDiscountParkAdapter ra;

    ImageButton toAddReportBtn;
    ListView myReportListView;
    TextView myReportNoTxt;


    public MyReportDiscountParkFragment (String id){
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_report_discount_park, container, false);

        myReportListView = view.findViewById(R.id.myReportListView);
        toAddReportBtn = view.findViewById(R.id.toAddReportBtn);
        myReportNoTxt = view.findViewById(R.id.myReportNoTxt);

        toAddReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addReportIntent = new Intent(getActivity(), AddReportDiscountParkActivity.class);
                addReportIntent.putExtra("loginId", loginId);
                startActivity(addReportIntent);
            }
        });

        myReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReportDiscountParkInformationIntent = new Intent(getActivity(), ReportDiscountParkInformationActivity.class);
                toReportDiscountParkInformationIntent.putExtra("id", loginId);
                toReportDiscountParkInformationIntent.putExtra("documentId", ((ReportDiscountParkItem) ra.getItem(position)).getDocumentId());
                startActivity(toReportDiscountParkInformationIntent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (ra != null) {
            ra.clearItem();
        }
        ra = new ReportDiscountParkAdapter(getActivity());

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadMyReports(loginId, new OnFirestoreDataLoadedListener() {
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

                MainActivity ma = (MainActivity) getActivity();
                if (ma != null) {
                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myReportListView.setAdapter(ra);

                            if (ra.getCount() == 0) {
                                myReportListView.setVisibility(View.GONE);
                                myReportNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                myReportListView.setVisibility(View.VISIBLE);
                                myReportNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}