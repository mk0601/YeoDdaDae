package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    boolean isChanging = false;
    FirebaseAuth mAuth;

    ImageButton changePasswordBackBtn;
    EditText changePasswordIdTxt;
    EditText changePasswordEmailTxt;
    ImageButton changePasswordSendEmailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        changePasswordBackBtn = findViewById(R.id.changePasswordBackBtn);
        changePasswordIdTxt = findViewById(R.id.changePasswordIdTxt);
        changePasswordEmailTxt = findViewById(R.id.changePasswordEmailTxt);
        changePasswordSendEmailBtn = findViewById(R.id.changePasswordSendEmailBtn);

        mAuth = FirebaseAuth.getInstance();

        changePasswordBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        changePasswordEmailTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    changePasswordSendEmailBtn.callOnClick();
                }

                return false;
            }
        });

        changePasswordSendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (isChanging) {
                    return;
                }

                isChanging = true;

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(changePasswordIdTxt.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(changePasswordEmailTxt.getWindowToken(), 0);
                }

                String id = replaceNewlinesAndTrim(changePasswordIdTxt);
                String email = replaceNewlinesAndTrim(changePasswordEmailTxt);

                if (id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                    isChanging = false;
                }
                else if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    isChanging = false;
                }
                else if (id.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "ID는 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    isChanging = false;
                }
                else if (email.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "이메일은 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    isChanging = false;
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                    isChanging = false;
                }
                else if (!isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 이메일 형식입니다", Toast.LENGTH_SHORT).show();
                    isChanging = false;
                }
                else {
                    FirestoreDatabase fd = new FirestoreDatabase();
                    fd.findEmailAndIsAdminById(id, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            String fDEmail = ((String[]) data)[0];

                            if (email.equals(fDEmail)) {
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "이메일을 확인하여 비밀번호 변경 후 다시 로그인하세요", Toast.LENGTH_SHORT).show();
                                                isChanging = false;
                                                finish();
                                            }
                                            else {
                                                Log.d(ContentValues.TAG, "인증 이메일 전송 실패 : " + task.getException().getMessage());
                                                Toast.makeText(getApplicationContext(), "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                                isChanging = false;
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "ID 계정의 이메일이 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                                isChanging = false;
                            }
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            isChanging = false;
                        }
                    });

                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
