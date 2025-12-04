package com.example.evee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class halamanhome extends Fragment implements MoodUpdatePopup.OnMoodSavedListener {

    private TextView textGreeting, textDate, textCycle, textCycleStatus, textMoodDesc, textMoodComment;
    private ImageView imageMoodIcon;
    private TextView textAvgCycleLength, textAvgPeriodLength, textCycleStatusLabel;

    private CardView cardMood;
    private Button buttonEditMood;

    private LinearLayout layoutUpcomingActivities;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halaman_home, container, false);

        // Inisialisasi view
        textGreeting = view.findViewById(R.id.textGreeting);
        textDate = view.findViewById(R.id.textDate);
        textCycle = view.findViewById(R.id.textCycle);
        textCycleStatus = view.findViewById(R.id.textCycleStatus);
        textMoodDesc = view.findViewById(R.id.textMoodDesc);
        textMoodComment = view.findViewById(R.id.textMoodComment);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        cardMood = view.findViewById(R.id.cardMood);
        layoutUpcomingActivities = view.findViewById(R.id.layoutUpcomingActivities);


        // Card "Siklus haid saya"
        textAvgCycleLength = view.findViewById(R.id.textAvgCycleLength);
        textAvgPeriodLength = view.findViewById(R.id.textAvgPeriodLength);
        textCycleStatusLabel = view.findViewById(R.id.textCycleStatusLabel);

        sessionManager = new SessionManager(requireContext());

        Button buttonLogPeriod = view.findViewById(R.id.buttonLogPeriod);

        buttonLogPeriod.setOnClickListener(v -> {
            // Kalau kalender berupa Fragment
            Fragment calendarFragment = new halamancalendar(); // ganti dengan nama fragment kalender kamu
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, calendarFragment) // sesuaikan container
                    .addToBackStack(null)
                    .commit();

            // Kalau kalender berupa Activity, pakai Intent:
            // Intent intent = new Intent(requireContext(), CalendarActivity.class);
            // startActivity(intent);
        });


        // Tanggal hari ini
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

        buttonEditMood = view.findViewById(R.id.buttonEditMood);

        buttonEditMood.setOnClickListener(v -> {
            MoodUpdatePopup popup = new MoodUpdatePopup(requireContext(), this);
            popup.show();
        });

        // Buka popup edit mood
        cardMood.setOnClickListener(v -> {
            MoodUpdatePopup popup = new MoodUpdatePopup(requireContext(), this);
            popup.show();
        });

        return view;
    }

    private String formatDateIndo(String dateRaw) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return sdfOutput.format(sdfInput.parse(dateRaw));
        } catch (Exception e) {
            return dateRaw; // fallback
        }
    }

    private void loadDashboard(String userId) {
        String url = ApiConfig.HOME_URL + "?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Jika perlu screening
                        if (response.has("needs_screening") && response.getBoolean("needs_screening")) {
                            JSONObject userObj = response.getJSONObject("user");
                            String userName = userObj.optString("name", "User");
                            textGreeting.setText("Halo, " + userName);

                            textCycle.setText("—");
                            textCycleStatus.setText("Belum ada data siklus");
                            textMoodDesc.setText("Belum ada mood");
                            textMoodComment.setText("Yuk, isi screening dulu!");
                            imageMoodIcon.setImageResource(R.drawable.normal);

                            // Kosongkan activity list
                            layoutUpcomingActivities.removeAllViews();
                            return;
                        }

                        // Success
                        if (response.getBoolean("success")) {
                            JSONObject userObj = response.getJSONObject("user");
                            JSONObject cycleObj = response.getJSONObject("cycle");

                            // Greeting
                            String userName = userObj.optString("name", "User");
                            textGreeting.setText("Halo, " + userName);

                            // Cycle data
                            int cycleDay          = cycleObj.getInt("cycle_day");
                            boolean isMens        = cycleObj.getBoolean("is_menstruating");
                            int daysToNext        = cycleObj.getInt("days_to_next_period");
                            String todayPhase     = cycleObj.getString("today_phase");
                            int cycleLengthRange  = cycleObj.getInt("cycle_length_range");
                            int periodLength      = cycleObj.getInt("period_length");

                            if (isMens) {
                                textCycle.setText("Menstruasi ke " + cycleDay + " Hari");
                            } else {
                                textCycle.setText("Menstruasi dalam: " + daysToNext + " Hari");
                            }
                            textCycleStatus.setText(
                                    "Siklus saat ini : " + todayPhase + " (Range: " + cycleLengthRange + ")"
                            );

                            // Card "Siklus haid saya" (rata-rata dan status)
                            textAvgCycleLength.setText(cycleLengthRange + " hari");
                            textAvgPeriodLength.setText(periodLength + " hari");
                            if (cycleLengthRange < 21 || cycleLengthRange > 35) {
                                textCycleStatusLabel.setText("Tidak Normal");
                                textCycleStatusLabel.setTextColor(0xFFFF0000);
                            } else {
                                textCycleStatusLabel.setText("Normal");
                                textCycleStatusLabel.setTextColor(0xFF5CB85C);
                            }

                            // Mood hari ini
                            if (!response.isNull("today_mood")) {
                                JSONObject moodObj = response.getJSONObject("today_mood");
                                textMoodDesc.setText(moodObj.getString("name"));
                                textMoodComment.setText("Mood hari ini: " + moodObj.getString("mood_tag"));

                                String iconFile = moodObj.optString("icon", "");
                                int resId = getLocalMoodIcon(iconFile);
                                imageMoodIcon.setImageResource(resId);
                                imageMoodIcon.setVisibility(View.VISIBLE);
                            } else {
                                textMoodDesc.setText("Belum ada mood");
                                textMoodComment.setText("Yuk, catat mood-mu hari ini!");
                                imageMoodIcon.setImageResource(R.drawable.normal);
                            }

                            // Upcoming activities (tanggal + keterangan, tanpa jam)
                            layoutUpcomingActivities.removeAllViews();
                            if (response.has("upcoming_activities")) {
                                JSONArray acts = response.getJSONArray("upcoming_activities");
                                for (int i = 0; i < acts.length(); i++) {
                                    JSONObject act = acts.getJSONObject(i);

                                    String dateRaw = act.optString("date", "");
                                    String title   = act.optString("title", "");

                                    String formattedDate = dateRaw.isEmpty() ? "-" : formatDateIndo(dateRaw);

                                    LinearLayout row = new LinearLayout(requireContext());
                                    row.setOrientation(LinearLayout.VERTICAL);
                                    row.setPadding(0, dp(6), 0, dp(6));

                                    TextView tvDate = new TextView(requireContext());
                                    tvDate.setText(formattedDate);
                                    tvDate.setTextColor(0xFF850E35);
                                    tvDate.setTextSize(14);
                                    tvDate.setPadding(0, 0, 0, dp(2));

                                    TextView tvTitle = new TextView(requireContext());
                                    tvTitle.setText(title);
                                    tvTitle.setTextColor(0xFF3F001D);
                                    tvTitle.setTextSize(14);

                                    row.addView(tvDate);
                                    row.addView(tvTitle);

                                    layoutUpcomingActivities.addView(row);

                                    View divider = new View(requireContext());
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                                    params.setMargins(0, dp(8), 0, dp(8));
                                    divider.setLayoutParams(params);
                                    divider.setBackgroundColor(0xFFDDDDDD);

                                    layoutUpcomingActivities.addView(divider);
                                }
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
    // Helper dp
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // Callback dari popup saat mood disimpan
    @Override
    public void onMoodSaved(String moodLabel, String moodId, String iconFile) {
        updateMoodUI(moodLabel, iconFile);
        saveMoodToServer(moodId, "");
    }

    // Update UI mood
    private void updateMoodUI(String moodLabel, String iconFile) {
        textMoodDesc.setText(moodLabel);
        textMoodComment.setText("Mood hari ini: " + moodLabel);

        int resId = getLocalMoodIcon(iconFile);
        imageMoodIcon.setImageResource(resId);
        imageMoodIcon.setVisibility(View.VISIBLE);
    }

    // Kirim mood ke server
    private void saveMoodToServer(String moodId, String note) {
        String url = ApiConfig.MOODSLOG_URL;

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(getContext(), "Mood berhasil disimpan", Toast.LENGTH_SHORT).show();
                    String userId = sessionManager.getUserId();
                    if (userId != null) loadDashboard(userId);
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

    // Map nama file icon dari DB ke drawable lokal (fallback ke getIdentifier)
    private int getLocalMoodIcon(String iconFileName) {
        if (iconFileName == null || iconFileName.isEmpty()) {
            return R.drawable.mascnormal;
        }

        // hapus ekstensi
        String baseName = iconFileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex != -1) baseName = baseName.substring(0, dotIndex);

        // coba cari drawable dengan nama persis
        int resId = requireContext().getResources()
                .getIdentifier(baseName, "drawable", requireContext().getPackageName());

        if (resId == 0) {
            // fallback ke normal jika tidak ditemukan
            resId = R.drawable.mascnormal;
        }
        return resId;
    }
}
