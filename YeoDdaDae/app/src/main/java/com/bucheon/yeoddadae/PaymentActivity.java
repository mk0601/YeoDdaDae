package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentActivity extends AppCompatActivity {
    String shareParkDocumentName;
    String loginId;
    HashMap<String, ArrayList<String>> reservationTime;
    int price;

    Button paymentBackBtn;
    TextView paymentTotalPriceContentTxt;
    HorizontalScrollView paymentMethodContentScrollView;
    Button paymentPayBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentBackBtn = findViewById(R.id.paymentBackBtn);
        paymentTotalPriceContentTxt = findViewById(R.id.paymentTotalPriceContentTxt);
        paymentMethodContentScrollView = findViewById(R.id.paymentMethodContentScrollView);
        paymentPayBtn = findViewById(R.id.paymentPayBtn);

        Intent inIntent = getIntent();
        shareParkDocumentName = inIntent.getStringExtra("shareParkDocumentName");
        loginId = inIntent.getStringExtra("id");
        reservationTime = (HashMap<String, ArrayList<String>>) inIntent.getSerializableExtra("time");
        price = inIntent.getIntExtra("price", -1);

        if (price == -1) {
            Log.d(TAG, "price값 오류");
            finish();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                paymentTotalPriceContentTxt.setText(Integer.toString(price));
            }
        });

        paymentBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        paymentPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreDatabase fd = new FirestoreDatabase();
                HashMap<String, Object> hm = new HashMap<>();
                hm.put("shareParkDocumentName", shareParkDocumentName);
                hm.put("id", loginId);
                hm.put("time", reservationTime);
                hm.put("isCancelled", false);
                hm.put("upTime", FieldValue.serverTimestamp());
                hm.put("price", price);

                fd.insertData("reservation", hm);
                Toast.makeText(getApplicationContext(), "예약되었습니다", Toast.LENGTH_SHORT);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);

                finish();
            }
        });
    }
}
