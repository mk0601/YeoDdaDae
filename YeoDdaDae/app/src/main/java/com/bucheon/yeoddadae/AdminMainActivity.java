package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;

public class AdminMainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    String loginId;

    ImageButton toApproveShareParkBtn;
    TextView toApproveShareParkNotificationTxt;
    ImageButton toApproveReportBtn;
    TextView toApproveReportNotificationTxt;
    ImageButton toStatisticsBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton menubarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        toApproveShareParkBtn = findViewById(R.id.toApproveShareParkBtn);
        toApproveShareParkNotificationTxt = findViewById(R.id.toApproveShareParkNotificationTxt);
        toApproveReportBtn = findViewById(R.id.toApproveReportBtn);
        toApproveReportNotificationTxt = findViewById(R.id.toApproveReportNotificationTxt);
        toStatisticsBtn = findViewById(R.id.toStatisticsBtn);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menubarBtn = findViewById(R.id.menubarBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderUsername = headerView.findViewById(R.id.nowIdTxt);
        if (loginId != null) {
            navHeaderUsername.setText(loginId);
        }

        menubarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_logout) {
                    loginId = null;
                    mAuth.signOut();
                    Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(logoutIntent);
                    finish();
                }
                else if (item.getItemId() == R.id.nav_change_password) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AdminMainActivity.this);
                    builder.setTitle("비밀번호 변경 확인");
                    builder.setMessage("정말 비밀번호를 변경하시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetPassword();
                        }
                    });
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(AdminMainActivity.this, R.color.sub));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AdminMainActivity.this, R.color.sub));
                }
                return false;
            }
        });

        toApproveShareParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent approveShareParkIntent = new Intent(getApplicationContext(), ApproveShareParkActivity.class);
                startActivity(approveShareParkIntent);
            }
        });

        toApproveReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent approveReportIntent = new Intent(getApplicationContext(), ApproveReportActivity.class);
                startActivity(approveReportIntent);
            }
        });

        toStatisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statisticsIntent = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(statisticsIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirestoreDatabase fd = new FirestoreDatabase();

        fd.loadAdminNotification(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                int [] result = (int[]) data;

                int approveShareParkNotification = result[0];
                int approveReportNotification = result[1];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat formatter = new DecimalFormat("#,###");

                        if (approveShareParkNotification == 0) {
                            toApproveShareParkNotificationTxt.setVisibility(View.GONE);
                        }
                        else {
                            toApproveShareParkNotificationTxt.setText(formatter.format(approveShareParkNotification));
                            toApproveShareParkNotificationTxt.setVisibility(View.VISIBLE);
                        }

                        if (approveReportNotification == 0) {
                            toApproveReportNotificationTxt.setVisibility(View.GONE);
                        }
                        else {
                            toApproveReportNotificationTxt.setText(formatter.format(approveReportNotification));
                            toApproveReportNotificationTxt.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void resetPassword() {
        mAuth.sendPasswordResetEmail(user.getEmail())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "이메일을 확인하여 비밀번호 변경 후 다시 로그인하세요", Toast.LENGTH_SHORT).show();
                        App app = (App) getApplication();
                        app.setLoginId(null);
                        loginId = null;
                        mAuth.signOut();
                        Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(logoutIntent);
                        finish();
                    }
                    else {
                        Log.d(ContentValues.TAG, "인증 이메일 전송 실패 : " + task.getException().getMessage());
                        Toast.makeText(getApplicationContext(), "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}