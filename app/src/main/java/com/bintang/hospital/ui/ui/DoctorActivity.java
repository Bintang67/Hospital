package com.bintang.hospital.ui.ui;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.bintang.hospital.R;

import org.json.JSONObject;

public class DoctorActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate(R.layout.activity_doctor, R.id.recyclerDoctors, R.id.btnAddDoctor,
                AddDoctorActivity.class, R.layout.item_doctor, this::bind);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        load("/doctors", R.id.progressDoctors, R.id.txtDoctorCount,
                "Doctor List (%d)", Toast.LENGTH_LONG);
    }

    private void load(String s, int progressDoctors, int txtDoctorCount, String s1, int lengthLong) {
    }

    private void bind(JsonAdapter.Row row, JSONObject doctor) {
        row.text(R.id.txtDoctorName, doctor.optString("name", "-"))
                .text(R.id.txtDoctorId, "ID: " + doctor.optString("doctor_id", "-"))
                .text(R.id.txtDoctorSpecialization,
                        "Specialization: " + doctor.optString("specialization", "-"))
                .text(R.id.txtDoctorPhone, "Phone: " + doctor.optString("phone_number", "-"))
                .text(R.id.txtDoctorBranch, "Branch: " + doctor.optString("hospital_branch", "-"))
                .click(R.id.btnEditDoctor, () -> startActivity(
                        new Intent(this, EditDoctorActivity.class)
                                .putExtra("doctor_data", doctor.toString())))
                .click(R.id.btnDeleteDoctor, () -> confirmDelete(
                        "Delete Doctor",
                        "Are you sure you want to delete " + doctor.optString("name", "Doctor") + "?",
                        "/doctors/" + Uri.encode(doctor.optString("doctor_id")),
                        "Doctor deleted successfully",
                        Toast.LENGTH_LONG,
                        this::load));
    }
}