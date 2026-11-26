package com.bintang.hospital.ui.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bintang.hospital.R;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.api.Result;
import com.google.firebase.inappmessaging.internal.ApiClient;

import org.chromium.base.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminActivity<RectListDebuggerModifierNode, JsonArray extends Result, JsonElement> extends AppCompatActivity {

    private static final int PAGE_SIZE = 100;

    private static class Fld {
        String key, label;
        boolean number, required, isId;

        Fld(String key, String label, boolean number,
            boolean required, boolean isId) {
            this.key = key;
            this.label = label;
            this.number = number;
            this.required = required;
            this.isId = isId;
        }
    }

    private static Fld f(String k, String l) {
        return new Fld(k, l, false, false, false);
    }

    private static Fld req(String k, String l) {
        return new Fld(k, l, false, true, false);
    }

    private static Fld id(String k, String l) {
        return new Fld(k, l, false, true, true);
    }

    private static Fld num(String k, String l) {
        return new Fld(k, l, true, false, false);
    }

    private static Fld idNum(String k, String l) {
        return new Fld(k, l, true, true, true);
    }

    private static class Res {
        String name, path, titleKey;
        String[] subKeys;
        Fld[] fields;

        Res(String name, String path, String titleKey,
            String[] subKeys, Fld[] fields) {
            this.name = name;
            this.path = path;
            this.titleKey = titleKey;
            this.subKeys = subKeys;
            this.fields = fields;
        }
    }

    private Res[] resources;
    private Res cur;

    private final List<JsonObject> items = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();

    private LinearLayout tabContainer, cardContainer;
    private TextView tvEmpty;

    private int gen = 0;
    private boolean loading = false;

    private Res[] buildResources() {
        return new Res[]{

                new Res("Pasien", "api/v1/patients", "name",
                        new String[]{"patient_id", "gender", "contact_number"},
                        new Fld[]{
                                id("patient_id", "ID Pasien"),
                                req("name", "Nama"),
                                f("gender", "Gender"),
                                f("date_of_birth", "Tgl Lahir"),
                                f("contact_number", "No. Kontak"),
                                f("address", "Alamat"),
                                f("insurance_provider", "Asuransi"),
                                f("registration_date", "Tgl Daftar")
                        }),

                new Res("Dokter", "api/v1/doctors", "name",
                        new String[]{"doctor_id", "specialization", "hospital_branch"},
                        new Fld[]{
                                id("doctor_id", "ID Dokter"),
                                req("name", "Nama"),
                                f("specialization", "Spesialisasi"),
                                f("phone_number", "No. Telepon"),
                                f("years_experience", "Pengalaman (tahun)"),
                                f("hospital_branch", "Cabang RS"),
                                f("email", "Email")
                        }),

                new Res("Staf", "api/v1/staff", "staff_name",
                        new String[]{"staff_id", "role", "service"},
                        new Fld[]{
                                id("staff_id", "ID Staf"),
                                req("staff_name", "Nama Staf"),
                                f("role", "Peran"),
                                f("service", "Layanan")
                        }),

                new Res("Tempat Tidur", "api/v1/hospital_beds", "service",
                        new String[]{"week", "month", "available_beds"},
                        new Fld[]{
                                idNum("week", "Minggu"),
                                idNum("month", "Bulan"),
                                id("service", "Layanan"),
                                num("available_beds", "Jumlah Bed Tersedia")
                        }),

                new Res("Tagihan", "api/v1/billing", "bill_id",
                        new String[]{"patient_id", "amount", "payment_status"},
                        new Fld[]{
                                id("bill_id", "ID Tagihan"),
                                req("patient_id", "ID Pasien"),
                                num("amount", "Jumlah Tagihan"),
                                f("payment_status", "Status Pembayaran")
                        })
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finishAffinity();
                    }
                });

        if (ApiClient.get() == null) {
            goLogin();
            return;
        }

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnLogout = findViewById(R.id.btnLogout);

        resources = buildResources();

        buildTabs();

        btnAdd.setOnClickListener(v -> showForm(null));
        btnLogout.setOnClickListener(v -> goLogin());

        selectTab(0);
    }

    private void buildTabs() {
        for (int i = 0; i < resources.length; i++) {
            final int index = i;

            Button button = new Button(this);
            button.setText(resources[i].name);
            button.setAllCaps(false);

            button.setOnClickListener(v -> selectTab(index));

            tabContainer.addView(button);
            tabButtons.add(button);
        }
    }

    private void selectTab(int index) {
        cur = resources[index];

        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setAlpha(i == index ? 1f : 0.5f);
        }

        load();
    }

    private void load() {
        gen++;
        loading = false;

        items.clear();
        refreshList();
        fetchData();
    }

    private void fetchData() {
        if (loading) return;

        loading = true;

        final Res target = cur;
        final int myGen = gen;

        tvEmpty.setText("Memuat...");
        tvEmpty.setVisibility(View.VISIBLE);

        ApiClient.get().list(target.path, 0, PAGE_SIZE)
                .enqueue(new Callback<>() {

                    @Override
                    public void zza() {

                    }

                    public void onResponse(
                            Call call,
                            Response<JsonArray> response) {

                        if (myGen != gen) return;

                        loading = false;

                        if (response.isSuccessful()
                                && response.body() != null) {

                            items.clear();

                            for (JsonElement element : response.body()) {
                                if (element.isJsonObject()
                                        && items.size() < PAGE_SIZE) {
                                    items.add(element.getAsJsonObject());
                                }
                            }

                            if (target.subKeys.length > 0) {
                                String sortKey = target.subKeys[0];

                                Collections.sort(items,
                                        (o1, o2) ->
                                                str(o1, sortKey).compareTo(
                                                        str(o2, sortKey)
                                                ));
                            }

                            refreshList();

                        } else {
                            tvEmpty.setText("Gagal memuat data");
                            handleError(response);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<JsonArray> call,
                            Throwable t) {

                        if (myGen != gen) return;

                        loading = false;
                        tvEmpty.setText("Tidak bisa terhubung ke server");
                    }
                });
    }

    private void refreshList() {
        cardContainer.removeAllViews();

        for (JsonObject item : items) {
            addCard(item);
        }

        if (items.isEmpty() && !loading) {
            tvEmpty.setText("Belum ada data");
            tvEmpty.setVisibility(View.VISIBLE);
        } else if (!items.isEmpty()) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void addCard(JsonObject item) {

        CardView card = new CardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));

        card.setLayoutParams(cardParams);
        card.setRadius(dp(12));
        card.setCardElevation(dp(3));
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setUseCompatPadding(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView title = new TextView(this);
        title.setText(str(item, cur.titleKey).isEmpty()
                ? "(tanpa nama)"
                : str(item, cur.titleKey));
        title.setTextSize(17);
        title.setTextColor(0xFF0F172A);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        content.addView(title);

        for (String key : cur.subKeys) {
            String value = str(item, key);

            if (value.isEmpty()) continue;

            TextView sub = new TextView(this);
            sub.setText(labelOf(key) + ": " + value);
            sub.setTextSize(13);
            sub.setTextColor(0xFF64748B);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            params.topMargin = dp(5);
            content.addView(sub, params);
        }

        TextView action = new TextView(this);
        action.setText("Ketuk untuk Edit / Hapus");
        action.setTextSize(12);
        action.setTextColor(0xFF0D9488);
        action.setGravity(android.view.Gravity.END);

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        actionParams.topMargin = dp(10);

        content.addView(action, actionParams);

        card.addView(content);

        card.setOnClickListener(v -> showActions(item));

        cardContainer.addView(card);
    }

    private String labelOf(String key) {
        for (Fld field : cur.fields) {
            if (field.key.equals(key)) {
                return field.label;
            }
        }

        return key;
    }

    private String rowText(JsonObject item) {
        StringBuilder text = new StringBuilder();

        text.append(str(item, cur.titleKey));

        for (String key : cur.subKeys) {
            String value = str(item, key);

            if (!value.isEmpty()) {
                text.append("\n")
                        .append(labelOf(key))
                        .append(": ")
                        .append(value);
            }
        }

        return text.toString();
    }

    private void showActions(JsonObject item) {
        new AlertDialog.Builder(this)
                .setTitle(str(item, cur.titleKey))
                .setItems(new String[]{"Edit", "Hapus"}, (dialog, which) -> {
                    if (which == 0) {
                        showForm(item);
                    } else {
                        confirmDelete(item);
                    }
                })
                .show();
    }

    private void showForm(JsonObject existing) {
        final boolean edit = existing != null;
        final Res target = cur;

        ScrollView scroll = new ScrollView(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(8), dp(20), 0);

        scroll.addView(box);

        EditText[] inputs = new EditText[target.fields.length];

        for (int i = 0; i < target.fields.length; i++) {
            Fld field = target.fields[i];

            EditText input = new EditText(this);
            input.setHint(field.label + (field.required ? " *" : ""));
            input.setSingleLine(true);

            input.setInputType(field.number
                    ? InputType.TYPE_CLASS_NUMBER
                    : InputType.TYPE_CLASS_TEXT);

            if (edit) {
                input.setText(str(existing, field.key));

                if (field.isId) {
                    input.setEnabled(false);
                }
            }

            box.addView(input);
            inputs[i] = input;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle((edit ? "Edit " : "Tambah ") + target.name)
                .setView(scroll)
                .setPositiveButton("Simpan", null)
                .setNegativeButton("Batal", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            save.setOnClickListener(v -> {
                JsonObject body = new JsonObject();

                for (int i = 0; i < target.fields.length; i++) {
                    Fld field = target.fields[i];

                    String value = inputs[i].getText().toString().trim();

                    if (edit && field.isId) continue;

                    if (value.isEmpty()) {
                        if (field.required) {
                            inputs[i].setError("Wajib diisi");
                            return;
                        }
                        continue;
                    }

                    if (field.number) {
                        try {
                            body.addProperty(field.key, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            inputs[i].setError("Harus berupa angka");
                            return;
                        }
                    } else {
                        body.addProperty(field.key, value);
                    }
                }

                save.setEnabled(false);

                Call<JsonObject> call = edit
                        ? ApiClient.get().update(itemPath(target, existing), body)
                        : ApiClient.get().create(target.path, body);

                call.enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(
                            Call<JsonObject> c,
                            Response<JsonObject> response) {

                        save.setEnabled(true);

                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            toast(edit ? "Data diperbarui" : "Data ditambahkan");
                            load();
                        } else {
                            handleError(response);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<JsonObject> c,
                            Throwable t) {

                        save.setEnabled(true);
                        toast("Tidak bisa terhubung ke server");
                    }
                });
            });
        });

        dialog.show();
    }

    private void confirmDelete(JsonObject item) {
        final Res target = cur;

        new AlertDialog.Builder(this)
                .setTitle("Hapus data ini?")
                .setMessage(rowText(item))
                .setPositiveButton("Hapus", (dialog, which) ->

                        ApiClient.get().delete(itemPath(target, item))
                                .enqueue(new Callback<JsonObject>() {

                                    @Override
                                    public void onResponse(
                                            Call<JsonObject> c,
                                            Response<JsonObject> response) {

                                        if (response.isSuccessful()) {
                                            toast("Data dihapus");
                                            load();
                                        } else {
                                            handleError(response);
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            Call<JsonObject> c,
                                            Throwable t) {

                                        toast("Tidak bisa terhubung ke server");
                                    }
                                }))
                .setNegativeButton("Batal", null)
                .show();
    }

    private String itemPath(Res resource, JsonObject item) {
        StringBuilder path = new StringBuilder(resource.path);

        for (Fld field : resource.fields) {
            if (field.isId) {
                path.append("/")
                        .append(Uri.encode(str(item, field.key)));
            }
        }

        return path.toString();
    }

    private static String str(JsonObject object, String key) {
        JsonElement element = object.get(key);

        return element == null || element.isJsonNull()
                ? ""
                : element.getAsString();
    }

    private void handleError(Response<?> response) {
        int code = response.code();

        if (code == 401) {
            toast("Sesi habis, silakan login lagi");
            goLogin();
            return;
        }

        if (code == 403) {
            toast("Akun tidak memiliki izin");
            return;
        }

        String detail = "";

        try {
            if (response.errorBody() != null) {
                JsonObject object = JsonParser.parseString(
                        response.errorBody().string()
                ).getAsJsonObject();

                if (object.has("detail")) {
                    detail = object.get("detail").getAsString();
                }
            }
        } catch (Exception ignored) {
        }

        toast("Gagal (kode " + code + ")"
                + (detail.isEmpty() ? "" : ": " + detail));
    }

    private void goLogin() {
        RectListDebuggerModifierNode ApiClient = null;
        ApiClient.setToken(null);

        Intent intent = new Intent(this, LoginActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}