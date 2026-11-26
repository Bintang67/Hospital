package com.bintang.hospital.ui.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.daffa.dokter2an.R;
import com.daffa.dokter2an.admin_dashboard.AdminActivity;
import com.daffa.dokter2an.admin_dashboard.ClientActivity;
import com.daffa.dokter2an.doctor_dashboard.DoctorDashboardActivity;
import com.daffa.dokter2an.nurse_dashboard.NurseDashboardActivity;
import com.daffa.dokter2an.receptionist_dashboard.ReceptionistDashboardActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername, inputPassword;
    private Button btnLogin;
    private ProgressBar progressLogin;
    private SessionActivity session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        session = new SessionActivity(this);
        String token = session.getToken();

        if (token != null && !token.isEmpty()) {
            setContentView(R.layout.activity_splash);
            verifikasiUser(token, false);
        } else {
            setupLoginScreen();
        }
    }

    private void setupLoginScreen() {
        setContentView(R.layout.activity_login);

        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);

        inputPassword.setOnTouchListener((v, event) -> {
            Drawable icon = inputPassword.getCompoundDrawables()[2];

            if (event.getAction() != MotionEvent.ACTION_UP || icon == null
                    || event.getRawX() < inputPassword.getRight()
                    - icon.getBounds().width() - inputPassword.getPaddingEnd()) {
                return false;
            }

            int cursor = inputPassword.getSelectionStart();
            boolean hidden = inputPassword.getTransformationMethod()
                    instanceof PasswordTransformationMethod;

            inputPassword.setTransformationMethod(hidden
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            inputPassword.setSelection(Math.max(0, cursor));
            return true;
        });

        btnLogin.setOnClickListener(v -> prosesLogin());
    }

    private void showLoginForm() {
        if (inputUsername == null) {
            setupLoginScreen();
        }
        setLoading(false);
    }

    private void prosesLogin() {
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString();

        if (username.isEmpty()) {
            inputUsername.setError("Username wajib diisi");
            inputUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            inputPassword.setError("Password wajib diisi");
            inputPassword.requestFocus();
            return;
        }

        setLoading(true);

        ClientActivity.login(username, password, new ClientActivity.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                String token = response.optString("access_token", "");

                runOnUiThread(() -> {
                    if (token.isEmpty()) {
                        setLoading(false);
                        showMessage("Token tidak ditemukan");
                    } else {
                        verifikasiUser(token, true);
                    }
                });
            }

            @Override
            public void onError(String message, int statusCode) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showMessage(message);
                });
            }
        });
    }

    private void verifikasiUser(String token, boolean saveToken) {
        PixelCopy ClientActivity;
        ClientActivity.request("/users/me", "GET", token, null, new ClientActivity.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Class<?> dashboard = dashboardFor(response.optString("username", ""));

                runOnUiThread(() -> {
                    if (dashboard == null) {
                        session.clearSession();
                        showLoginForm();
                        showMessage("Role akun tidak dikenali");
                        return;
                    }

                    if (saveToken) {
                        session.saveToken(token);
                    }

                    startActivity(new Intent(LoginActivity.this, dashboard).setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                });
            }

            @Override
            public void onError(String message, int statusCode) {
                runOnUiThread(() -> {
                    boolean expired = statusCode == 401 || statusCode == 403;

                    if (expired) {
                        session.clearSession();
                    }

                    showLoginForm();
                    showMessage(expired
                            ? "Sesi tidak valid atau sudah kedaluwarsa. Silakan login kembali."
                            : message);
                });
            }
        });
    }

    private Class<?> dashboardFor(String username) {
        switch (username.toLowerCase()) {
            case "admin":
                return com.bintang.hospital.ui.ui.AdminActivity.class;
        }
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}