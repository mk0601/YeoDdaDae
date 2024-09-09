package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MyYdPointFragment extends Fragment {
    String loginId;
    long ydPoint;
    int spinnerStatus;
    boolean isFirstSpinnerCalled = false;
    String startDate;
    String startTime;
    String endDate;
    String endTime;
    Timestamp startTs;
    Timestamp endTs;
    YdPointHistoryAdapter ypha;
    FirestoreDatabase fd;

    TextView myYdPointTxt;
    ImageButton toChargeYdPointImgBtn;
    ImageButton toRefundYdPointImgBtn;
    Spinner pointSpinner;
    ConstraintLayout pointHistoryCustomTimeConstLayout;
    EditText pointHistoryCustomTimeStartDateEditTxt;
    EditText pointHistoryCustomTimeStartTimeEditTxt;
    EditText pointHistoryCustomTimeEndDateEditTxt;
    EditText pointHistoryCustomTimeEndTimeEditTxt;
    ListView pointHistoryListView;
    TextView pointHistoryNoTxt;

    public MyYdPointFragment(String id) {
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_yd_point, container, false);

        myYdPointTxt = view.findViewById(R.id.myYdPointTxt);
        toChargeYdPointImgBtn = view.findViewById(R.id.toChargeYdPointImgBtn);
        toRefundYdPointImgBtn = view.findViewById(R.id.toRefundYdPointImgBtn);
        pointSpinner = view.findViewById(R.id.pointSpinner);
        pointHistoryCustomTimeConstLayout = view.findViewById(R.id.pointHistoryCustomTimeConstLayout);
        pointHistoryCustomTimeStartDateEditTxt = view.findViewById(R.id.pointHistoryCustomTimeStartDateEditTxt);
        pointHistoryCustomTimeStartTimeEditTxt = view.findViewById(R.id.pointHistoryCustomTimeStartTimeEditTxt);
        pointHistoryCustomTimeEndDateEditTxt = view.findViewById(R.id.pointHistoryCustomTimeEndDateEditTxt);
        pointHistoryCustomTimeEndTimeEditTxt = view.findViewById(R.id.pointHistoryCustomTimeEndTimeEditTxt);
        pointHistoryListView = view.findViewById(R.id.pointHistoryListView);
        pointHistoryNoTxt = view.findViewById(R.id.pointHistoryNoTxt);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.my_spinner_point_history_items,
                R.layout.my_spinner_2
        );
        pointSpinner.setAdapter(adapter);

        toChargeYdPointImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chargeYdPointIntent = new Intent(getActivity(), YdPointChargeActivity.class);
                chargeYdPointIntent.putExtra("loginId", loginId);
                startActivity(chargeYdPointIntent);
            }
        });

        toRefundYdPointImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent refundYdPointIntent = new Intent(getActivity(), YdPointRefundActivity.class);
                refundYdPointIntent.putExtra("loginId", loginId);
                startActivity(refundYdPointIntent);
            }
        });

        pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                pointHistoryCustomTimeConstLayout.setVisibility(View.GONE);

                startDate = null;
                startTime = null;
                endDate = null;
                endTime = null;

                pointHistoryCustomTimeStartDateEditTxt.setText("");
                pointHistoryCustomTimeStartTimeEditTxt.setText("");
                pointHistoryCustomTimeEndDateEditTxt.setText("");
                pointHistoryCustomTimeEndTimeEditTxt.setText("");

                if (selectedItem.equals("전체")) {
                    spinnerStatus = 2;
                    if (!isFirstSpinnerCalled) {
                        isFirstSpinnerCalled = true;
                    }
                    else {
                        getYpPointHistory();
                    }
                }
                else if (selectedItem.equals("증가만")) {
                    spinnerStatus = 3;
                    getYpPointHistory();
                }
                else if (selectedItem.equals("감소만")) {
                    spinnerStatus = 4;
                    getYpPointHistory();
                }
                else if (selectedItem.equals("기간 직접 설정")) {
                    spinnerStatus = 5;
                    if (ypha != null) {
                        ypha.clear();
                    }
                    pointHistoryCustomTimeConstLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        pointHistoryCustomTimeStartDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(getActivity(), MyYdPointFragment.this, 0);
                sdpd.show();
            }
        });

        pointHistoryCustomTimeStartTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String timeFormat;
                        int amPmHour;
                        if (selectedHour >= 12) {
                            timeFormat = "오후";
                            if (selectedHour > 12) {
                                amPmHour = selectedHour - 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        } else {
                            timeFormat = "오전";
                            if (selectedHour == 0) {
                                amPmHour = 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        }
                        String formattedTime = String.format(Locale.getDefault(), "%s %02d:%02d", timeFormat, amPmHour, selectedMinute);
                        pointHistoryCustomTimeStartTimeEditTxt.setText(formattedTime);
                        startTime = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
                        try {
                            checkAllCustomTimeSetted();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, 0, false); // is24HourView를 false로 설정

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        pointHistoryCustomTimeEndDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(getActivity(), MyYdPointFragment.this, 1);
                sdpd.show();
            }
        });

        pointHistoryCustomTimeEndTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String timeFormat;
                        int amPmHour;
                        if (selectedHour >= 12) {
                            timeFormat = "오후";
                            if (selectedHour > 12) {
                                amPmHour = selectedHour - 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        } else {
                            timeFormat = "오전";
                            if (selectedHour == 0) {
                                amPmHour = 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        }
                        String formattedTime = String.format(Locale.getDefault(), "%s %02d:%02d", timeFormat, amPmHour, selectedMinute);
                        pointHistoryCustomTimeEndTimeEditTxt.setText(formattedTime);
                        endTime = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
                        try {
                            checkAllCustomTimeSetted();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, 0, false); // is24HourView를 false로 설정

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        pointHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                YdPointHistoryItem selectedItem = (YdPointHistoryItem) ypha.getItem(position);

                if (selectedItem.getReservationId() != null && !selectedItem.getReservationId().isEmpty()) {
                    Intent toReservationInformationIntent = new Intent(getActivity(), ReservationInformationActivity.class);
                    toReservationInformationIntent.putExtra("id", loginId);
                    toReservationInformationIntent.putExtra("documentId", selectedItem.reservationId);
                    startActivity(toReservationInformationIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fd = new FirestoreDatabase();
        fd.loadYdPoint(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ydPoint = (long) data;
                String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint);
                myYdPointTxt.setText(formattedYdPoint);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getActivity().getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });

        getYpPointHistory();
    }

    void checkAllCustomTimeSetted () throws ParseException {
        if (startDate != null && startTime != null && endDate != null && endTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);

            Date startDateTime = dateFormat.parse(startDate + startTime);
            startTs = new Timestamp(startDateTime);

            Date endDateTime = dateFormat.parse(endDate + endTime);
            endTs = new Timestamp(endDateTime);

            getYpPointHistory();
        }
    }

    void getYpPointHistory() {
        fd.loadYdPointHistory(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                if (ypha != null) {
                    ypha.clear();
                }

                ypha = new YdPointHistoryAdapter();

                ArrayList<ArrayList<HashMap<String, Object>>> result = (ArrayList<ArrayList<HashMap<String, Object>>>) data;
                ArrayList<HashMap<String, Object>> charge = result.get(0);
                ArrayList<HashMap<String, Object>> refund = result.get(1);
                ArrayList<HashMap<String, Object>> spend = result.get(2);
                ArrayList<HashMap<String, Object>> receive = result.get(3);

                for (HashMap<String, Object> hm : charge) {
                    ypha.addItem(new YdPointHistoryItem("충전", (long) hm.get("chargedYdPoint"), null, null, null, null, null, (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                }
                for (HashMap<String, Object> hm : refund) {
                    ypha.addItem(new YdPointHistoryItem("환급", (long) hm.get("refundedYdPoint"), (String) hm.get("bank"), (String) hm.get("accountNumber"), null, null, null, (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                }
                for (HashMap<String, Object> hm : spend) {
                    if ((long) hm.get("price") != 0) {
                        ypha.addItem(new YdPointHistoryItem("사용", (long) hm.get("price"), null, null, (String) hm.get("type"), (String) hm.get("reservationId"), null, (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                    }
                }
                for (HashMap<String, Object> hm : receive) {
                    if ((long) hm.get("receivedYdPoint") != 0) {
                        ypha.addItem(new YdPointHistoryItem("받음", (long) hm.get("receivedYdPoint"), null, null, null, null, (String) hm.get("type"), (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                    }
                }

                if (spinnerStatus == 3) {
                    ypha.onlyPlus();
                }
                if (spinnerStatus == 4) {
                    ypha.onlyMinus();
                }
                if(spinnerStatus == 5) {
                    ypha.customPeriod(startTs, endTs);
                }

                ypha.sortByUpTime();

                MainActivity ma = (MainActivity) getActivity();
                if (ma != null) {
                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pointHistoryListView.setAdapter(ypha);

                            if (ypha.getCount() == 0) {
                                pointHistoryListView.setVisibility(View.GONE);
                                pointHistoryNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                pointHistoryListView.setVisibility(View.VISIBLE);
                                pointHistoryNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });
    }

    public void setStartDate(String startDate) throws ParseException {
        this.startDate = startDate;
        pointHistoryCustomTimeStartDateEditTxt.setText(startDate.substring(0, 4) + "년 " + startDate.substring(4, 6) + "월 " + startDate.substring(6) + "일");
        checkAllCustomTimeSetted();
    }

    public void setEndDate(String endDate) throws ParseException {
        this.endDate = endDate;
        pointHistoryCustomTimeEndDateEditTxt.setText(endDate.substring(0, 4) + "년 " + endDate.substring(4, 6) + "월 " + endDate.substring(6) + "일");
        checkAllCustomTimeSetted();
    }
}