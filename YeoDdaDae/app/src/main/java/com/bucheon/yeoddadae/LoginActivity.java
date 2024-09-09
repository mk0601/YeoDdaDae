package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    boolean isLogining = false;
    FirebaseAuth mAuth;

    ImageButton backBtn;
    EditText idTxt;
    EditText pwTxt;
    ImageButton loginBtn;
    ImageButton registerBtn;
    TextView toChangePasswordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        backBtn = findViewById(R.id.loginBackBtn);
        idTxt = findViewById(R.id.loginIdTxt);
        pwTxt = findViewById(R.id.loginPwTxt);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.toRegisterBtn);
        toChangePasswordTxt = findViewById(R.id.toChangePasswordTxt);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        pwTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    loginBtn.callOnClick();
                }

                return false;
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (isLogining) {
                    return;
                }

                isLogining = true;

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(idTxt.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(pwTxt.getWindowToken(), 0);
                }

                String id = replaceNewlinesAndTrim(idTxt);
                String pw = replaceNewlinesAndTrim(pwTxt);

                if (id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                    isLogining = false;
                }
                else if (pw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    isLogining = false;
                }
                else if (id.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "ID는 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    isLogining = false;
                }
                else if (pw.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    isLogining = false;
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                    isLogining = false;
                }
                else if (pw.length() <= 5 || pw.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                    isLogining = false;
                }
                else {
                    FirestoreDatabase fd = new FirestoreDatabase();

                    fd.findEmailAndIsAdminById(id, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            String email = ((String[]) data)[0];
                            boolean isAdmin = Boolean.parseBoolean(((String[]) data)[1]);

                            mAuth.signInWithEmailAndPassword(email, pw)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                                            App app = (App) getApplication();
                                            app.setLoginId(id);
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("loginId", id);
                                            resultIntent.putExtra("isAdmin", isAdmin);
                                            setResult(RESULT_OK, resultIntent);
                                            isLogining = false;
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                                            isLogining = false;
                                            pwTxt.setText("");
                                        }
                                    });
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            if (errorMessage.equals("계정이 없음")) {
                                Toast.makeText(getApplicationContext(), "해당 아이디의 계정이 없습니다", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            }
                            pwTxt.setText("");
                            isLogining = false;
                        }
                    });
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        toChangePasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePwIntent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePwIntent);
            }
        });
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
