package kr.ac.duksung.mycol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;

public class LoginActivity extends AppCompatActivity {

    private EditText login_email, login_pw;
    private CheckBox checkBoxAuto;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tzalogo);

        // Firebase 초기화
        FirebaseApp.initializeApp(this);

        firebaseAuth = FirebaseAuth.getInstance();
        login_email = findViewById(R.id.editTextEmail);
        login_pw = findViewById(R.id.editTextPw);
        checkBoxAuto = findViewById(R.id.checkBoxAuto);

        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 자동 로그인 정보 확인
        checkAutoLogin();
    }

    public void onLoginButtonClick(View view) {
        String email = login_email.getText().toString().trim();
        String password = login_pw.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // 로그인 성공
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                showToast("로그인 성공");

                                // 체크박스 상태에 따라 이메일과 비밀번호 저장
                                if (checkBoxAuto.isChecked()) {
                                    editor.putBoolean("autoLogin", true);
                                    editor.putString("email", email);
                                    editor.putString("password", password);
                                    editor.apply();
                                } else {
                                    editor.clear();
                                    editor.apply();
                                }

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // 로그인 실패
                                showToast("이메일이나 비밀번호를 다시 확인해주세요.");
                            }
                        }
                    });
        } else {
            showToast("이메일과 비밀번호를 입력하세요.");
        }
    }

    public void onSignupButtonClick(View view) {
        // SignupActivity로 연결
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    private void checkAutoLogin() {
        boolean autoLogin = sharedPreferences.getBoolean("autoLogin", false);
        if (autoLogin) {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");

            if (!email.isEmpty() && !password.isEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 자동 로그인 성공
                                    showToast("자동 로그인 성공");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 자동 로그인 실패
                                    showToast("자동 로그인 실패. 이메일과 비밀번호를 확인해주세요.");
                                }
                            }
                        });
            }
        }
    }

    private void showToast(String message) {
        // 커스텀 토스트 레이아웃 인플레이트
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

        // 메시지를 설정
        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        // 토스트 생성 및 표시
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
