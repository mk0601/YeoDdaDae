package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    final int PERMISSION_REQUEST_CODE = 1;
    final int LOGIN_INTENT_REQUEST_CODE = 2;
    String loginId;
    boolean isAdmin;
    boolean isSkip;

    ImageButton toLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        toLoginBtn = findViewById(R.id.toLoginBtn);

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    isSkip = false;
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(loginIntent, LOGIN_INTENT_REQUEST_CODE);
                }
                else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
            loginId = data.getStringExtra("loginId");
            isAdmin = data.getBooleanExtra("isAdmin", false);
            checkPermission(); // 권한 확인. 결과 처리는 onRequestPermissionsResult에서 진행
        }
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(ContentValues.TAG, "권한 이미 있음");
            proceedAfterPermissionGranted();
        }
        else {
            Log.d(ContentValues.TAG, "권한 없음. 요청");
            String[] permissionArr = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissionArr, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Log.d(TAG, "권한요청에서 허가");
                proceedAfterPermissionGranted();
            } else {
                Toast.makeText(getApplicationContext(), "필수 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void proceedAfterPermissionGranted() {
        Intent intent;

        if (!isSkip) {
            if (isAdmin) {
                intent = new Intent(getApplicationContext(), AdminMainActivity.class);
            }
            else {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            }
            intent.putExtra("loginId", loginId);
            intent.putExtra("isAdmin", isAdmin);
        }
        else {
            loginId = "user111";
            isAdmin = false;

            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("loginId", loginId);
            intent.putExtra("isAdmin", isAdmin);
        }

        startActivity(intent);
        finish();
    }
}
