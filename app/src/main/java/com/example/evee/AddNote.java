package com.example.evee;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;   // tambahkan import ini
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddNote extends Fragment {

    LinearLayout layoutMiniCalendar;
    TextView txtSelectedDate;
    EditText edtNote;
    Button btnSaveNote;

    Calendar selectedDateCalendar;
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // format API

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_note, container, false);

        layoutMiniCalendar = view.findViewById(R.id.layoutMiniCalendar);
        txtSelectedDate = view.findViewById(R.id.txtSelectedDate);
        edtNote = view.findViewById(R.id.edtNote);
        btnSaveNote = view.findViewById(R.id.btnSaveNote);

        // Default tanggal hari ini
        selectedDateCalendar = Calendar.getInstance();
        txtSelectedDate.setText(
                new SimpleDateFormat("dd MMMM yyyy", new Locale("id"))
                        .format(selectedDateCalendar.getTime())
        );

        generateMiniCalendar(inflater);

        btnSaveNote.setOnClickListener(v -> saveActivity());

        return view;
    }

    private void generateMiniCalendar(LayoutInflater inflater) {
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Calendar itemDate = (Calendar) calendar.clone();

            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(60), dp(60));
            params.setMargins(0,0,dp(10),0);
            item.setLayoutParams(params);
            item.setBackgroundResource(R.drawable.bg_calendar_normal);

            TextView txtDay = new TextView(requireContext());
            TextView txtDate = new TextView(requireContext());

            SimpleDateFormat dayFormat = new SimpleDateFormat("E", new Locale("id"));
            String day = dayFormat.format(itemDate.getTime()).substring(0,1).toUpperCase();
            int tgl = itemDate.get(Calendar.DAY_OF_MONTH);

            txtDay.setText(day);
            txtDate.setText(String.valueOf(tgl));

            txtDay.setTextColor(Color.BLACK);
            txtDate.setTextColor(Color.BLACK);
            txtDay.setTextSize(12);
            txtDate.setTextSize(16);
            txtDay.setGravity(Gravity.CENTER);
            txtDate.setGravity(Gravity.CENTER);

            item.addView(txtDay);
            item.addView(txtDate);

            // Tandai hari ini
            Calendar today = Calendar.getInstance();
            if(itemDate.get(Calendar.YEAR)==today.get(Calendar.YEAR) &&
                    itemDate.get(Calendar.MONTH)==today.get(Calendar.MONTH) &&
                    itemDate.get(Calendar.DAY_OF_MONTH)==today.get(Calendar.DAY_OF_MONTH)) {
                item.setBackgroundResource(R.drawable.bg_calendar_selected);
            }

            item.setOnClickListener(v -> {
                selectedDateCalendar = (Calendar) itemDate.clone();
                txtSelectedDate.setText(
                        new SimpleDateFormat("dd MMMM yyyy", new Locale("id"))
                                .format(selectedDateCalendar.getTime())
                );
                // Reset background semua item
                for (int j=0;j<layoutMiniCalendar.getChildCount();j++){
                    layoutMiniCalendar.getChildAt(j).setBackgroundResource(R.drawable.bg_calendar_normal);
                }
                item.setBackgroundResource(R.drawable.bg_calendar_selected);
            });

            layoutMiniCalendar.addView(item);
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
    }

    private void saveActivity(){
        if(selectedDateCalendar==null){
            Toast.makeText(requireContext(), "Pilih tanggal terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

        String titleText = edtNote.getText().toString().trim();
        if(titleText.isEmpty()){
            Toast.makeText(requireContext(), "Judul aktivitas kosong.", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateStr = SDF.format(selectedDateCalendar.getTime());
        String userId = new SessionManager(requireContext()).getUserId();
        if(userId == null){
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("title", titleText);          // WAJIB
            body.put("description", "");           // opsional
            body.put("date", dateStr);             // WAJIB
            body.put("start_time", "13:00:00");    // WAJIB, format HH:mm:ss
            body.put("end_time", JSONObject.NULL); // kirim null, bukan string kosong
            body.put("category", "Pribadi");
            body.put("priority", "Sedang");
            body.put("status", "Belum");
        } catch (Exception e) { e.printStackTrace(); }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.NOTES_URL,   // arahkan ke activities.php
                body,
                response -> {
                    Log.d("ACTIVITIES", "Response: " + response.toString());
                    Toast.makeText(getContext(), "Aktivitas tersimpan ✅", Toast.LENGTH_SHORT).show();
                    edtNote.setText("");

                    // balik ke fragment kalender
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, new halamancalendar()) // ganti dengan fragment kalender kamu
                            .commit();
                }
                ,
                error -> {
                    Toast.makeText(getContext(), "Gagal simpan aktivitas", Toast.LENGTH_SHORT).show();
                    if (error.networkResponse != null) {
                        Log.e("ACTIVITIES", "Code: " + error.networkResponse.statusCode);
                        Log.e("ACTIVITIES", "Resp: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("ACTIVITIES", "Volley error: " + error.toString());
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
