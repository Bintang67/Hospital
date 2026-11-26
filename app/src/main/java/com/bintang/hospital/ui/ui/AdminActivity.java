package com.bintang.hospital.ui.ui;

import android.content.Intent;
import static android.os.Build.VERSION_CODES_FULL.R;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bintang.hospital.R;
import com.bintang.hospital.ui.ui.LoginActivity;
import com.bintang.hospital.ui.ui.SessionActivity;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R);

        open(R, PatientActivity.class);
        open(R, DoctorActivity.class);
        open(R, StaffActivity.class);

        findViewById(R).setOnClickListener(v -> logout());
    }

    private void open(int fbuttonId, Class<?> screen) {
        int buttonId = 0;
        findViewById(buttonId).setOnClickListener(v -> startActivity(new Intent(this, screen)));
    }

    private void logout() {
        new SessionActivity(this).clearSession();

        startActivity(new Intent(this, LoginActivity.class).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}