package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FieldValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    String payType;
    String shareParkDocumentName;
    String loginId;
    HashMap<String, ArrayList<String>> reservationTime;
    int price;
    long ydPoint;
    int defaultTextColor;
    FirestoreDatabase fd;

    ImageButton paymentBackBtn;
    TextView paymentTotalPriceContentTxt;
    TextView paymentYdPointContentTxt;
    TextView paymentAfterPayPointContentTxt;
    TextView paymentAfterPayPointPtTxt;
    ImageButton paymentPayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentBackBtn = findViewById(R.id.paymentBackBtn);
        paymentTotalPriceContentTxt = findViewById(R.id.paymentTotalPriceContentTxt);
        paymentYdPointContentTxt = findViewById(R.id.paymentYdPointContentTxt);
        paymentAfterPayPointContentTxt = findViewById(R.id.paymentAfterPayPointContentTxt);
        paymentAfterPayPointPtTxt = findViewById(R.id.paymentAfterPayPointPtTxt);
        paymentPayBtn = findViewById(R.id.paymentPayBtn);

        Intent inIntent = getIntent();
        shareParkDocumentName = inIntent.getStringExtra("shareParkDocumentName");
        payType = inIntent.getStringExtra("payType");
        loginId = inIntent.getStringExtra("id");
        reservationTime = (HashMap<String, ArrayList<String>>) inIntent.getSerializableExtra("time");
        price = inIntent.getIntExtra("price", -1);

        if (price == -1) {
            Log.d(TAG, "price값 오류");
            finish();
        }

        defaultTextColor = paymentAfterPayPointContentTxt.getCurrentTextColor();

        fd = new FirestoreDatabase();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
                builder.setTitle("결제 확인");
                builder.setMessage("결제하시겠습니까");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fd = new FirestoreDatabase();
                        fd.payByYdPoint(loginId, price, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                HashMap<String, Object> hm = new HashMap<>();
                                hm.put("shareParkDocumentName", shareParkDocumentName);
                                hm.put("id", loginId);
                                hm.put("time", reservationTime);
                                hm.put("isCancelled", false);
                                hm.put("upTime", FieldValue.serverTimestamp());
                                hm.put("price", price);
                                fd.insertData("reservation", hm, new OnFirestoreDataLoadedListener() {
                                    @Override
                                    public void onDataLoaded(Object data) {
                                        String reservationDocumentId = (String) data;

                                        HashMap<String, Object> hm = new HashMap<>();
                                        hm.put("id", loginId);
                                        hm.put("type", payType);
                                        hm.put("reservationId", reservationDocumentId);
                                        hm.put("price", price);
                                        hm.put("upTime", FieldValue.serverTimestamp());
                                        fd.insertData("spendYdPointHistory", hm, new OnFirestoreDataLoadedListener() {
                                            @Override
                                            public void onDataLoaded(Object data) {
                                                Toast.makeText(getApplicationContext(), "예약되었습니다", Toast.LENGTH_SHORT).show();

                                                Intent returnIntent = new Intent();
                                                setResult(RESULT_OK, returnIntent);

                                                finish();
                                            }

                                            @Override
                                            public void onDataLoadError(String errorMessage) {
                                                Log.d(TAG, errorMessage);
                                                Toast.makeText(getApplicationContext(), "소비 문서 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onDataLoadError(String errorMessage) {
                                        Log.d(TAG, errorMessage);
                                        Toast.makeText(getApplicationContext(), "예약 문서 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                if (errorMessage.equals("포인트가 부족합니다")) {
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(PaymentActivity.this);
                                    builder2.setTitle("포인트가 부족합니다");
                                    builder2.setMessage("충전하시겠습니까?");
                                    builder2.setPositiveButton("예", (dialog, which) -> {
                                        Intent ydPointChargeIntent = new Intent(getApplicationContext(), YdPointChargeActivity.class);
                                        ydPointChargeIntent.putExtra("loginId", loginId);
                                        startActivity(ydPointChargeIntent);
                                    });
                                    builder2.setNegativeButton("아니오", (dialog, which) -> dialog.dismiss());
                                    AlertDialog dialog2 = builder2.create();
                                    dialog2.show();

                                    dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.sub));
                                    dialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.sub));
                                } else {
                                    Log.d(TAG, errorMessage);
                                    Toast.makeText(getApplicationContext(), "결제 시도 중 오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.sub));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        fd.loadYdPoint(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ydPoint = (long) data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint);
                        String formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(price);
                        long afterPayYdPoint = ydPoint - price;
                        String formattedAfterPay = NumberFormat.getNumberInstance(Locale.KOREA).format(afterPayYdPoint);

                        paymentYdPointContentTxt.setText(formattedYdPoint);
                        paymentTotalPriceContentTxt.setText(formattedPrice);
                        paymentAfterPayPointContentTxt.setText(formattedAfterPay);

                        if (afterPayYdPoint < 0) {
                            int redColor = Color.rgb(255, 64, 64);

                            paymentAfterPayPointContentTxt.setTextColor(redColor);
                            paymentAfterPayPointPtTxt.setTextColor(redColor);
                        }
                        else if (afterPayYdPoint == 0) {
                            paymentAfterPayPointContentTxt.setTextColor(defaultTextColor);
                            paymentAfterPayPointPtTxt.setTextColor(defaultTextColor);
                        }
                        else if (afterPayYdPoint > 0) {
                            int blueColor = Color.rgb(64, 64, 255);

                            paymentAfterPayPointContentTxt.setTextColor(blueColor);
                            paymentAfterPayPointPtTxt.setTextColor(blueColor);
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
