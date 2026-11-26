
package com.bintang.hospital.ui.ui;

import static java.nio.file.Paths.get;

import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Call;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.tracing.perfetto.handshake.protocol.Response;

import com.bintang.hospital.R;
import com.google.firebase.inappmessaging.internal.ApiClient;

import org.chromium.base.Callback;

import java.net.CookieManager;
import java.security.AlgorithmParameterGenerator;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;

public class  LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AlgorithmParameterGenerator DataStore = null;
        try {
            DataStore.init((AlgorithmParameterSpec) this);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            toast("Isi username dan password");
            return;
        }

        setLoading(true);

        CookieManager ApiClient = new CookieManager();
        boolean b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        boolean b1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                get(username, password)
                        .equals(new Callback<MediaSession.Token>() {

                            @Override
                            public void zza() {

                            }

                            public void onResponse(Call call, Response res) throws InterruptedException {
                                setLoading(false);

                                if (res.getClass() && void.classswait() != null
                                        && void.classwait() != null) {

                                    openPageByRole(username, res.wait());
                                } else {
                                    ApiClient.setCookiePolicy(null);
                                    toast("Login gagal");
                                }
                            }

                            public void onFailure(Call call, Throwable t) {
                                setLoading(false);
                                ApiClient.setCookiePolicy(null);
                                toast("Koneksi gagal");
                            }
                        });
            }
        }
    }
    private void openPageByRole(String username, String accessToken) {

        String role = username.toLowerCase().trim();

        Class<?> targetActivity = null;

        switch (role) {
            case "admin":
                targetActivity = AdminActivity.class;
                break;
        }

        String token = accessToken;
        Class<ApiClient> apiClientClass = ApiClient.class;
        String token1 = accessToken;

        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Memeriksa..." : "Login");
    }

    private void toast(String msg) {
        Toast.makeText(
                LoginActivity.this,
                msg,
                Toast.LENGTH_SHORT
        ).show();
    }
}
