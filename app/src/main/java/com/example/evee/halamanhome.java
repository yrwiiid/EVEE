package com.example.evee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class halamanhome extends Fragment {

    private TextView textGreeting, textDate, textCycle, textMoodEmoji, textMoodDesc;
    private LinearLayout calendarContainer;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halaman_home, container, false);

        textGreeting = view.findViewById(R.id.textGreeting);
        textDate = view.findViewById(R.id.textDate);
        textCycle = view.findViewById(R.id.textCycle);
        textMoodEmoji = view.findViewById(R.id.textMoodEmoji);
        textMoodDesc = view.findViewById(R.id.textMoodDesc);
        calendarContainer = view.findViewById(R.id.calendarContainer);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            textGreeting.setText(name != null && !name.isEmpty()
                    ? "Halo, " + name + "!"
                    : "Halo, pengguna!");
        }

        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        textDate.setText(todayDate);

        generateCalendar();

        if (currentUser != null) {
            loadCycleData(currentUser.getUid());
            loadMoodData(currentUser.getUid());
        }

        return view;
    }

    // 🔹 Ambil data siklus terbaru
    private void loadCycleData(String uid) {
        firestore.collection("users")
                .document(uid)
                .collection("cycles")
                .orderBy("startDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        textCycle.setText("Belum ada data siklus");
                        return;
                    }

                    DocumentSnapshot doc = query.getDocuments().get(0);
                    String startDateStr = doc.getString("startDate");
                    String endDateStr = doc.getString("endDate");

                    firestore.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                int avgCycle = userDoc.contains("averageCycleLength")
                                        ? userDoc.getLong("averageCycleLength").intValue() : 28;
                                int avgDuration = userDoc.contains("averageDuration")
                                        ? userDoc.getLong("averageDuration").intValue() : 5;

                                try {
                                    if (startDateStr == null) {
                                        textCycle.setText("Data siklus tidak lengkap");
                                        return;
                                    }

                                    Date startDate = sdf.parse(startDateStr);
                                    Calendar today = Calendar.getInstance();
                                    long diffDays = (today.getTimeInMillis() - startDate.getTime()) / (1000 * 60 * 60 * 24);

                                    if (diffDays < avgDuration) {
                                        // sedang haid
                                        textCycle.setText("Sedang menstruasi 💧 (hari ke-" + (diffDays + 1) + ")");
                                    } else if (diffDays >= 13 && diffDays <= 16) {
                                        // masa subur
                                        textCycle.setText("Masa subur 🌸");
                                    } else if (diffDays >= avgCycle) {
                                        // haid berikutnya diperkirakan
                                        Calendar nextStart = Calendar.getInstance();
                                        nextStart.setTime(startDate);
                                        nextStart.add(Calendar.DAY_OF_MONTH, avgCycle);

                                        Calendar nextEnd = (Calendar) nextStart.clone();
                                        nextEnd.add(Calendar.DAY_OF_MONTH, avgDuration - 1);

                                        String startNext = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(nextStart.getTime());
                                        String endNext = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(nextEnd.getTime());
                                        textCycle.setText("Menstruasi berikutnya: " + startNext + " - " + endNext);
                                    } else {
                                        textCycle.setText("Siklus berjalan normal ✨");
                                    }
                                } catch (Exception e) {
                                    textCycle.setText("Gagal membaca data siklus");
                                }
                            });
                })
                .addOnFailureListener(e -> textCycle.setText("Gagal memuat data: " + e.getMessage()));
    }

    // 🔹 Ambil mood
    private void loadMoodData(String uid) {
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        firestore.collection("Users")
                .document(uid)
                .collection("Mood")
                .document(today)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String mood = document.getString("mood");
                        if (mood != null) {
                            switch (mood) {
                                case "Senang 😊":
                                    textMoodEmoji.setText("😊");
                                    textMoodDesc.setText("Senang");
                                    break;
                                case "Biasa 😐":
                                    textMoodEmoji.setText("😐");
                                    textMoodDesc.setText("Biasa aja");
                                    break;
                                case "Sedih 😢":
                                    textMoodEmoji.setText("😢");
                                    textMoodDesc.setText("Sedih");
                                    break;
                                default:
                                    textMoodEmoji.setText("❓");
                                    textMoodDesc.setText("Mood tidak diketahui");
                                    break;
                            }
                        }
                    } else {
                        textMoodEmoji.setText("😐");
                        textMoodDesc.setText("Belum ada mood hari ini");
                    }
                });
    }

    // 🔹 Kalender horizontal mini
    private void generateCalendar() {
        calendarContainer.removeAllViews();

        Calendar now = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);

        for (int i = -3; i <= 3; i++) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String dayName = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());

            LinearLayout dayBox = new LinearLayout(getContext());
            dayBox.setOrientation(LinearLayout.VERTICAL);
            dayBox.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 0, 16, 0);
            dayBox.setLayoutParams(params);

            int pad = (int) (8 * getResources().getDisplayMetrics().density);
            dayBox.setPadding(pad, pad, pad, pad);
            dayBox.setMinimumWidth((int) (48 * getResources().getDisplayMetrics().density));

            if (calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)
                    && calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                    && calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                dayBox.setBackgroundResource(R.drawable.bg_today);
            } else {
                dayBox.setBackgroundResource(R.drawable.bg_normal_day);
            }

            TextView dayNumber = new TextView(getContext());
            dayNumber.setText(String.valueOf(day));
            dayNumber.setTextSize(16);
            dayNumber.setTextColor(Color.parseColor("#6A0D4A"));
            dayNumber.setGravity(Gravity.CENTER);

            TextView dayText = new TextView(getContext());
            dayText.setText(dayName);
            dayText.setTextSize(10);
            dayText.setTextColor(Color.parseColor("#B0AFAF"));
            dayText.setGravity(Gravity.CENTER);

            dayBox.addView(dayNumber);
            dayBox.addView(dayText);
            calendarContainer.addView(dayBox);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
