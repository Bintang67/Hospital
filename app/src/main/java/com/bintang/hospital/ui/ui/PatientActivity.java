package com.bintang.hospital.ui.ui;

import static androidx.core.app.PendingIntentCompat.send;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daffa.dokter2an.R;

import org.json.JSONArray;

public class PatientActivity extends BaseActivity {

    private RecyclerView recyclerPatients;
    private ProgressBar progressPatients;
    private TextView txtEmpty, txtPatientCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        recyclerPatients = findViewById(R.id.recyclerPatients);
        progressPatients = findViewById(R.id.progressPatients);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtPatientCount = findViewById(R.id.txtPatientCount);

        recyclerPatients.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddPatient).setOnClickListener(v ->
                startActivity(new Intent(this, AddPatientActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }

    private void loadPatients() {
        progressPatients.setVisibility(View.VISIBLE);
        recyclerPatients.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        Object ClientActivity;
        ClientActivity.requestArray("/patients", "GET", token(), new ClientActivity.ApiArrayCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                runOnUiThread(() -> {
                    boolean empty = response.length() == 0;

                    progressPatients.setVisibility(View.GONE);
                    txtPatientCount.setText("Patient List (" + response.length() + ")");
                    txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerPatients.setVisibility(empty ? View.GONE : View.VISIBLE);

                    if (!empty) {
                        recyclerPatients.setAdapter(new PatientAdapter(PatientActivity.this, response));
                    }
                });
            }

            @Override
            public void onError(String message, int statusCode) {
                runOnUiThread(() -> {
                    progressPatients.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                    txtEmpty.setText(message);
                    toast("Gagal memuat pasien");
                });
            }
        });
    }

    private Object token() {
    }

    public void confirmDeletePatient(String patientId, String patientName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Yakin ingin menghapus data pasien " + patientName + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    toast("Menghapus pasien...");
                    send("/patients/" + Uri.encode(patientId), "DELETE", null,
                            "Patient berhasil dihapus", "Gagal menghapus pasien: ",
                            Toast.LENGTH_LONG, this::loadPatients, null);
                })
                .show();
    }

    private void toast(String s) {
    }
}