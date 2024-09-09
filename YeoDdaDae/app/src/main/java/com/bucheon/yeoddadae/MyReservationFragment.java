package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReservationFragment extends Fragment {
    String loginId;
    ReservationAdapter ra;

    ListView myReservationListView;
    TextView myReservationNoTxt;

    public MyReservationFragment (String id) {
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_reservation, container, false);

        myReservationListView = view.findViewById(R.id.myReservationListView);
        myReservationNoTxt = view.findViewById(R.id.myReservationNoTxt);

        myReservationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReservationInformationActivityIntent = new Intent(getActivity(), ReservationInformationActivity.class);
                toReservationInformationActivityIntent.putExtra("id", loginId);
                toReservationInformationActivityIntent.putExtra("documentId", ((ReservationItem) ra.getItem(position)).getDocumentId());
                startActivity(toReservationInformationActivityIntent);
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
        ra = new ReservationAdapter(getActivity());

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadMyReservations(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myReservations = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneReservation : myReservations) {
                    String id = (String) oneReservation.get("id");
                    boolean isCancelled = (boolean) oneReservation.get("isCancelled");
                    String shareParkDocumentName = (String) oneReservation.get("shareParkDocumentName");
                    HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) oneReservation.get("time");
                    Timestamp upTime = (Timestamp) oneReservation.get("upTime");
                    String documentId = (String) oneReservation.get("documentId");

                    ra.addItem(new ReservationItem(id, isCancelled, shareParkDocumentName, reservationTime, upTime, documentId));
                }

                MainActivity ma = (MainActivity) getActivity();
                if (ma != null) {
                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myReservationListView.setAdapter(ra);

                            if (ra.getCount() == 0) {
                                myReservationListView.setVisibility(View.GONE);
                                myReservationNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                myReservationListView.setVisibility(View.VISIBLE);
                                myReservationNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}