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

public class MyShareParkFragment extends Fragment {
    String loginId;
    ShareParkAdapter spa;

    ImageButton myShareParkAddBtn;
    ListView myShareParkListView;
    TextView myShareParkNoTxt;

    public MyShareParkFragment(String id) {
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_share_park, container, false);

        myShareParkAddBtn = view.findViewById(R.id.myShareParkAddBtn);
        myShareParkListView = view.findViewById(R.id.myShareParkListView);
        myShareParkNoTxt = view.findViewById(R.id.myShareParkNoTxt);

        myShareParkAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareParkIntent = new Intent(getActivity(), ShareParkActivity.class);
                shareParkIntent.putExtra("loginId", loginId);
                startActivity(shareParkIntent);
            }
        });

        myShareParkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toMyShareParkInformationIntent = new Intent(getActivity(), MyShareParkInformationActivity.class);
                toMyShareParkInformationIntent.putExtra("id", loginId);
                toMyShareParkInformationIntent.putExtra("documentId", ((ShareParkItem) spa.getItem(position)).getDocumentId());
                startActivity(toMyShareParkInformationIntent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (spa != null) {
            spa.clearItem();
        }

        spa = new ShareParkAdapter(getActivity());

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.calculateFreeSharePark(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                FirestoreDatabase fd2 = new FirestoreDatabase();
                fd2.loadMyShareParks(loginId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        ArrayList<HashMap<String, Object>> myShareParks = (ArrayList<HashMap<String, Object>>) data;

                        for (HashMap<String, Object> oneSharePark : myShareParks) {
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

                            spa.addItem(new ShareParkItem(null, lat, lon, parkDetailAddress, isApproval, isCancelled, isCalculated, price, time, upTime, documentId));
                        }

                        MainActivity ma = (MainActivity) getActivity();
                        if (ma != null) {
                            ma.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myShareParkListView.setAdapter(spa);

                                    if (spa.getCount() == 0) {
                                        myShareParkListView.setVisibility(View.GONE);
                                        myShareParkNoTxt.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        myShareParkListView.setVisibility(View.VISIBLE);
                                        myShareParkNoTxt.setVisibility(View.GONE);
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

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}