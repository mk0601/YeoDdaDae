package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import org.threeten.bp.LocalDate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareParkActivity extends AppCompatActivity {
    final int gpsIntentRequestCode = 1;
    String loginId;

    double lat = 0;
    double lon = 0;

    TimeAdapter ta;

    ImageButton shareParkBackBtn;
    TextView parkNewAddressContentTxt;
    TextView parkOldAddressContentTxt;
    ImageButton gpsBtn;
    EditText parkDetailAddressEditTxt;
    EditText sharerNameEditTxt;
    EditText sharerPhoneEditTxt;
    EditText sharerEmailEditTxt;
    EditText sharerRelationEditTxt;
    EditText parkPriceEditTxt;
    ImageButton resetBtn;
    MaterialCalendarView parkDateCalendar;
    ListView parkTimeListView;
    ImageButton registrationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_park);

        shareParkBackBtn = findViewById(R.id.shareParkBackBtn);
        parkNewAddressContentTxt = findViewById(R.id.parkNewAddressContentTxt);
        parkOldAddressContentTxt = findViewById(R.id.parkOldAddressContentTxt);
        gpsBtn = findViewById(R.id.gpsBtn);
        parkDetailAddressEditTxt = findViewById(R.id.parkDetailAddressEditTxt);
        sharerNameEditTxt = findViewById(R.id.sharerNameEditTxt);
        sharerPhoneEditTxt = findViewById(R.id.sharerPhoneEditTxt);
        sharerEmailEditTxt = findViewById(R.id.sharerEmailEditTxt);
        sharerRelationEditTxt = findViewById(R.id.sharerRelationEditTxt);
        parkPriceEditTxt = findViewById(R.id.parkPriceEditTxt);
        resetBtn = findViewById(R.id.resetBtn);
        parkDateCalendar = findViewById(R.id.parkDateCalendar);
        parkTimeListView = findViewById(R.id.parkTimeListView);
        registrationBtn = findViewById(R.id.registrationBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        ta = new TimeAdapter();
        parkTimeListView.setAdapter(ta);

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gpsIntent = new Intent(getApplicationContext(), GpsCertificationActivity.class);
                startActivityForResult(gpsIntent, gpsIntentRequestCode);
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CalendarDay element : ta.clear()) {
                    parkDateCalendar.setDateSelected(element, false);
                }
                setTotalHeightofListView(parkTimeListView);
            }
        });

        parkDateCalendar.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        parkDateCalendar.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                // CalendarDay라는 클래스는 LocalDate 클래스를 기반으로 만들어진 클래스다
                // 때문에 MaterialCalendarView에서 연/월 보여주기를 커스텀하려면 CalendarDay 객체의 getDate()로 연/월을 구한 다음 LocalDate 객체에 넣어서
                // LocalDate로 변환하는 처리가 필요하다
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append(" ")
                        .append(calendarHeaderElements[1]);
                return calendarHeaderBuilder.toString();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay currentCalendarDay = CalendarDay.from(currentYear, currentMonth + 1, currentDay);

        calendar.add(Calendar.DAY_OF_MONTH, 99);
        int futureYear = calendar.get(Calendar.YEAR);
        int futureMonth = calendar.get(Calendar.MONTH);
        int futureDay = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay futureCalendarDay = CalendarDay.from(futureYear, futureMonth + 1, futureDay);

        parkDateCalendar.state().edit()
                .setMinimumDate(currentCalendarDay)
                .setMaximumDate(futureCalendarDay)
                .commit();

        parkDateCalendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    Log.d(TAG, "선택한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.addItem(new TimeItem(date));
                    ta.sortByDate();
                    setTotalHeightofListView(parkTimeListView);
                }
                else {
                    Log.d(TAG, "선택 해제 한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.removeItem(ta.findItem(date));
                    setTotalHeightofListView(parkTimeListView);
                }
            }
        });

        shareParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parkDetailAddress = replaceNewlinesAndTrim(parkDetailAddressEditTxt);
                String ownerName = replaceNewlinesAndTrim(sharerNameEditTxt);
                String ownerPhone = replaceNewlinesAndTrim(sharerPhoneEditTxt);
                String ownerEmail = replaceNewlinesAndTrim(sharerEmailEditTxt);
                String ownerParkingRelation = replaceNewlinesAndTrim(sharerRelationEditTxt);
                String priceText = replaceNewlinesAndTrim(parkPriceEditTxt);

                int price;

                if (lat == 0 || lon == 0) {
                    Toast.makeText(getApplicationContext(), "GPS로 주소를 찾으세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (parkDetailAddress.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "상세주소를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (ownerName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "공유자명을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (ownerPhone.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!isValidPhoneNumber(ownerPhone)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 전화번호입니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (ownerEmail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!isValidEmail(ownerEmail)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 이메일 형식입니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (ownerParkingRelation.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "주차장과의 관계를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (priceText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "가격을 설정하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    price = Integer.parseInt(priceText);
                }
                catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "가격을 숫자로 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ta.getAllTime() == null) {
                    Toast.makeText(getApplicationContext(), "시간설정이 잘못되었습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isShareTimeAfterNow()) {
                    Toast.makeText(getApplicationContext(), "현재시각 이후로 공유하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> hm = new HashMap<>();
                hm.put("ownerId", loginId);
                hm.put("lat", lat);
                hm.put("lon", lon);
                hm.put("time", ta.getAllTime());
                hm.put("parkDetailAddress", parkDetailAddress);
                hm.put("ownerName", ownerName);
                hm.put("ownerPhone", ownerPhone);
                hm.put("ownerEmail", ownerEmail);
                hm.put("ownerParkingRelation", ownerParkingRelation);
                hm.put("price", price);
                hm.put("upTime", FieldValue.serverTimestamp());
                hm.put("isApproval", false);
                hm.put("isCancelled", false);
                hm.put("cancelReason", null);
                hm.put("isCalculated", false);

                FirestoreDatabase fd = new FirestoreDatabase();
                fd.insertData("sharePark", hm, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "공유주차장 등록 신청되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "공유주차장 등록 신청 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^010[0-9]{8}$"; // 010으로 시작하고, 그 뒤에 8자리 숫자가 온다는 정규식
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isShareTimeAfterNow() {
        boolean isAfterNow = true;

        Calendar ca = Calendar.getInstance();

        int year = ca.get(Calendar.YEAR);
        int month = ca.get(Calendar.MONTH) + 1;
        int day = ca.get(Calendar.DAY_OF_MONTH);
        int hour = ca.get(Calendar.HOUR_OF_DAY);
        int minute = ca.get(Calendar.MINUTE);

        String nowString = "";
        nowString += year;
        if (month < 10) {
            nowString += "0";
        }
        nowString += month;
        if (day < 10) {
            nowString += "0";
        }
        nowString += day;
        if (hour < 10) {
            nowString += "0";
        }
        nowString += hour;
        if (minute < 10) {
            nowString += "0";
        }
        nowString += minute;
        Log.d(TAG, "현재 시각 : " + nowString);

        ta.sortByDate();
        CalendarDay shareTimeFirstDay = ((TimeItem) ta.getItem(0)).getDate();

        int shareYear = shareTimeFirstDay.getYear();
        int shareMonth = shareTimeFirstDay.getMonth();
        int shareDay = shareTimeFirstDay.getDay();

        String shareStartTimeString = "";

        shareStartTimeString += shareYear;
        if (shareMonth < 10) {
            shareStartTimeString += "0";
        }
        shareStartTimeString += shareMonth;
        if (shareDay < 10) {
            shareStartTimeString += "0";
        }
        shareStartTimeString += shareDay + ((TimeItem) ta.getItem(0)).getStartTime();
        Log.d(TAG, "공유 처음 시간 : " + shareStartTimeString);

        if (Long.parseLong(nowString) <= Long.parseLong(shareStartTimeString)) {
            isAfterNow = false;
        }

        return isAfterNow;
    }

    public void setTotalHeightofListView(ListView listView) {
        int numberOfItems = ta.getCount();
        int itemHeight = dpToPx(60); // 아이템 높이를 dp 단위로 변환하여 사용

        // Calculate total height of all items.
        int totalItemsHeight = numberOfItems * itemHeight;

        // Calculate total height of all item dividers.
        int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

        // Set list height.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == gpsIntentRequestCode) {
            if (resultCode == RESULT_OK) {
                lat = data.getDoubleExtra("lat", 0);
                lon = data.getDoubleExtra("lon", 0);

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding(lat, lon, "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");

                                    parkNewAddressContentTxt.setText(adrresses[2]);
                                    parkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
