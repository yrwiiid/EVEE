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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
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

        // POPUP EDIT MOOD (manual override)
        if (buttonEditMood != null) {
            buttonEditMood.setOnClickListener(v -> {
                MoodUpdatePopup popup = new MoodUpdatePopup(getContext(), (moodLabel, moodEmoji) -> {
                    updateMoodUI(moodLabel);
                    Toast.makeText(getContext(), "Mood disimpan (lokal)", Toast.LENGTH_SHORT).show();
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
                            // User belum isi screening
                            textGreeting.setText("Halo, " + response.getJSONObject("user").getString("name"));
                            textCycle.setText("—");
                            textCycleStatus.setText("Belum ada data siklus");
                            textMoodDesc.setText("Belum ada mood");
                            textMoodComment.setText("Yuk, isi screening dulu!");
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
                            } else {
                                textMoodDesc.setText("Belum ada mood");
                                textMoodComment.setText("Yuk, catat mood-mu hari ini!");
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


    // UPDATE UI MOOD (manual override)
    private void updateMoodUI(String moodLabel) {
        textMoodDesc.setText(moodLabel);
        textMoodComment.setText("Mood hari ini: " + moodLabel);
        imageMoodIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMoodSaved(String moodLabel, String moodEmoji) {
        updateMoodUI(moodLabel);
    }
}
