package com.example.evee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class halamanhome extends Fragment implements MoodUpdatePopup.OnMoodSavedListener {

    private TextView textGreeting, textDate, textCycle, textCycleStatus, textMoodDesc, textMoodComment;
    private ImageView imageMoodIcon;
    private CardView cardMood;
    private Button buttonEditMood;
    private Button buttonLogPeriod;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halaman_home, container, false);

        // INISIALISASI VIEW
        textGreeting = view.findViewById(R.id.textGreeting);
        textDate = view.findViewById(R.id.textDate);
        textCycle = view.findViewById(R.id.textCycle);
        textCycleStatus = view.findViewById(R.id.textCycleStatus);
        textMoodDesc = view.findViewById(R.id.textMoodDesc);
        textMoodComment = view.findViewById(R.id.textMoodComment);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        cardMood = view.findViewById(R.id.cardMood);
        buttonEditMood = view.findViewById(R.id.buttonEditMood);
        buttonLogPeriod = view.findViewById(R.id.buttonLogPeriod);

        sessionManager = new SessionManager(requireContext());

        // SET TANGGAL
        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        textDate.setText(todayDate);

        // Panggil API Dashboard
        String userId = sessionManager.getUserId();
        if (userId != null) {
            loadDashboard(userId);
        } else {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
        }

        // TOMBOL BUKA KALENDER
        if (buttonLogPeriod != null) {
            buttonLogPeriod.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, new halamancalendar())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // POPUP EDIT MOOD
        if (buttonEditMood != null) {
            buttonEditMood.setOnClickListener(v -> {
                MoodUpdatePopup popup = new MoodUpdatePopup(getContext(), (moodLabel, moodId, iconFile) -> {
                    // update UI langsung
                    updateMoodUI(moodLabel, iconFile);
                    // simpan ke server
                    saveMoodToServer(moodId, "");
                });
                popup.show();
            });
        }

        return view;
    }

    private void loadDashboard(String userId) {
        String url = ApiConfig.HOME_URL + "?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.has("needs_screening") && response.getBoolean("needs_screening")) {
                            textGreeting.setText("Halo, " + response.getJSONObject("user").getString("name"));
                            textCycle.setText("—");
                            textCycleStatus.setText("Belum ada data siklus");
                            textMoodDesc.setText("Belum ada mood");
                            textMoodComment.setText("Yuk, isi screening dulu!");
                            imageMoodIcon.setImageResource(R.drawable.percayadiri);
                            return;
                        }

                        if (response.getBoolean("success")) {
                            JSONObject userObj = response.getJSONObject("user");
                            JSONObject cycleObj = response.getJSONObject("cycle");

                            textGreeting.setText("Halo, " + userObj.getString("name"));
                            textCycle.setText(cycleObj.getInt("cycle_day") + " Hari");
                            textCycleStatus.setText("Siklus saat ini : " + cycleObj.getString("today_phase")
                                    + " (Range: " + cycleObj.getString("cycle_length_range") + ")");

                            if (!response.isNull("today_mood")) {
                                JSONObject moodObj = response.getJSONObject("today_mood");
                                textMoodDesc.setText(moodObj.getString("name"));
                                textMoodComment.setText("Mood hari ini: " + moodObj.getString("mood_tag"));

                                String iconFile = moodObj.getString("icon");
                                int resId = getMoodIconRes(iconFile);
                                imageMoodIcon.setImageResource(resId);
                                imageMoodIcon.setVisibility(View.VISIBLE);
                            } else {
                                textMoodDesc.setText("Belum ada mood");
                                textMoodComment.setText("Yuk, catat mood-mu hari ini!");
                                imageMoodIcon.setImageResource(R.drawable.percayadiri);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("HOME_DEBUG", "JSON parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("HOME_DEBUG", "Volley Error = " + error.toString());
                    Toast.makeText(getContext(), "Error koneksi", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    // Kirim mood ke API mood_logs.php
    private void saveMoodToServer(String moodId, String note) {
        String url = ApiConfig.MOODSLOG_URL; // pastikan nama file benar: mood_logs.php

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(getContext(), "Mood berhasil disimpan", Toast.LENGTH_SHORT).show();
                    loadDashboard(sessionManager.getUserId()); // refresh dashboard
                },
                error -> {
                    Toast.makeText(getContext(), "Gagal simpan mood", Toast.LENGTH_SHORT).show();
                    Log.e("MOOD_LOG", "Error: " + error.toString());
                }
        ) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("user_id", sessionManager.getUserId());
                    jsonBody.put("mood_id", moodId);
                    jsonBody.put("note", note);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(requireContext()).add(postRequest);
    }

    // Mapping nama file dari DB ke drawable Android
    private int getMoodIconRes(String iconFileName) {
        switch (iconFileName) {
            case "masc-senang.png": return R.drawable.senang;
            case "m002.png": return R.drawable.sedih;
            case "masc-cemas.png": return R.drawable.cemas;
            case "masc-capek.png": return R.drawable.lelah;
            case "m005.png": return R.drawable.bingung;
            case "masc-sensitif.png": return R.drawable.bosan;
            default: return R.drawable.percayadiri;
        }
    }

    // UPDATE UI MOOD
    private void updateMoodUI(String moodLabel, String iconFile) {
        textMoodDesc.setText(moodLabel);
        textMoodComment.setText("Mood hari ini: " + moodLabel);

        int resId = getMoodIconRes(iconFile);
        imageMoodIcon.setImageResource(resId);
        imageMoodIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMoodSaved(String moodLabel, String moodId, String iconFile) {
        updateMoodUI(moodLabel, iconFile);
        saveMoodToServer(moodId, "");
    }
}
