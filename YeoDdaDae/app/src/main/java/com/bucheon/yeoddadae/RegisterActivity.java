package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    FirestoreDatabase fd;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Handler handler;
    boolean isRegisterSuccessed = false;
    boolean isRegistering = false;

    ImageButton backBtn;
    EditText idTxt;
    EditText emailTxt;
    EditText pwTxt;
    ImageButton registerBtn;
    TextView registerEmailVerificationTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        backBtn = findViewById(R.id.registerBackBtn);
        idTxt = findViewById(R.id.registerIdTxt);
        emailTxt = findViewById(R.id.registerEmailTxt);
        pwTxt = findViewById(R.id.registerPwTxt);
        registerBtn = findViewById(R.id.registerBtn);
        registerEmailVerificationTxt = findViewById(R.id.registerEmailVerificationTxt);

        fd = new FirestoreDatabase();

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        handler = new Handler();

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
                    registerBtn.callOnClick();
                }

                return false;
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRegistering) {
                    return;
                }

                isRegistering = true;
                registerBtn.setEnabled(false);
                registerBtn.setImageResource(R.drawable.disabled_button);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(idTxt.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(emailTxt.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(pwTxt.getWindowToken(), 0);
                }

                String id = replaceNewlinesAndTrim(idTxt);
                String email = replaceNewlinesAndTrim(emailTxt);
                String pw = replaceNewlinesAndTrim(pwTxt);

                if (id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (pw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (id.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "ID는 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (email.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "이메일은 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (pw.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 공백을 포함할 수 없습니다", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자이어야 합니다", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (!isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 이메일 형식입니다", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else if (pw.length() <= 5 || pw.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6~20자이어야 합니다", Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.gradate_button);
                    isRegistering = false;
                }
                else{
                    fd.duplicationCheck("account", "id", id, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            if ((boolean) data) {
                                registerUser(id, email, pw);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show();
                                registerBtn.setEnabled(true);
                                registerBtn.setImageResource(R.drawable.gradate_button);
                                isRegistering = false;
                            }
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            registerBtn.setEnabled(true);
                            registerBtn.setImageResource(R.drawable.gradate_button);
                            isRegistering = false;
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isRegisterSuccessed) {
            if (user != null) {
                user.delete();
            }
        }
        mAuth.signOut();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void registerUser(String id, String email, String pw) {
        mAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "이메일 인증하세요", Toast.LENGTH_SHORT).show();
                                            checkEmailVerification(id, email, user);
                                        }
                                        else {
                                            Log.d(TAG, "인증 이메일 전송 실패 : " + verificationTask.getException().getMessage());
                                            Toast.makeText(getApplicationContext(), "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            registerBtn.setEnabled(true);
                                                            registerBtn.setImageResource(R.drawable.gradate_button);
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                    else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getApplicationContext(), "이미 존재하는 이메일입니다", Toast.LENGTH_SHORT).show();
                            registerBtn.setEnabled(true);
                            registerBtn.setImageResource(R.drawable.gradate_button);
                        }
                        else {
                            Log.d(TAG, "계정 생성 실패" + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "계정 생성 실패", Toast.LENGTH_SHORT).show();
                            registerBtn.setEnabled(true);
                            registerBtn.setImageResource(R.drawable.gradate_button);
                        }
                    }
                });
    }

    private void checkEmailVerification(String id, String email, FirebaseUser user) {
        registerEmailVerificationTxt.setVisibility(View.VISIBLE);
        final int[] timeLeft = {180}; //초 단위
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeft[0] > 0) {
                    user.reload().addOnSuccessListener(aVoid -> {
                        if (user.isEmailVerified()) {
                            Log.d(TAG, "이메일 인증 성공");
                            fd.register(id, email, user.getUid(), new OnFirestoreDataLoadedListener() {
                                @Override
                                public void onDataLoaded(Object data) {
                                    Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                    isRegisterSuccessed = true;
                                    finish();
                                }

                                @Override
                                public void onDataLoadError(String errorMessage) {
                                    Log.d(TAG, errorMessage);
                                    Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    registerBtn.setEnabled(true);
                                                    registerBtn.setImageResource(R.drawable.gradate_button);
                                                }
                                            });
                                }
                            });
                        }
                        else { // 다음 확인을 위해 시간 감소 및 재실행
                            Log.d(TAG, "남은 시간 초 : " + timeLeft[0]);
                            registerEmailVerificationTxt.setText(timeLeft[0] + "초 내에 이메일 인증해주세요");
                            timeLeft[0]--;
                            handler.postDelayed(this, 1000);
                        }
                    });
                }
                else { // 5분 동안 인증을 완료하지 못했을 때의 처리
                    Log.d(TAG, "이메일 인증 시간 초과");
                    registerEmailVerificationTxt.setText("이메일 인증 시간 초과되었습니다");
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    registerBtn.setEnabled(true);
                                    registerBtn.setImageResource(R.drawable.gradate_button);
                                }
                            });
                }
            }
        };
        handler.post(runnable);
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}