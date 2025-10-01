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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class halamanhome extends Fragment {

    private TextView textGreeting, textDate, textCycle, textMoodEmoji, textMoodDesc;
    private LinearLayout calendarContainer;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halaman_home, container, false);

        // Inisialisasi view
        textGreeting = view.findViewById(R.id.textGreeting);
        textDate = view.findViewById(R.id.textDate);
        textCycle = view.findViewById(R.id.textCycle);
        textMoodEmoji = view.findViewById(R.id.textMoodEmoji);
        textMoodDesc = view.findViewById(R.id.textMoodDesc);
        calendarContainer = view.findViewById(R.id.calendarContainer);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 🔹 Greeting
        if (currentUser != null) {
            String uid = currentUser.getUid();
            firestore.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = currentUser.getDisplayName();
                    textGreeting.setText(name != null && !name.isEmpty() ? "Halo, " + name + "!" : "Halo, pengguna!");
                }
            });
        }

        // 🔹 Tanggal hari ini
        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        textDate.setText(todayDate);

        // 🔹 Generate kalender horizontal
        generateCalendar();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // 🔹 Ambil siklus dari RTDB
            dbRef.child(uid).child("cycle").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String startDateStr = snapshot.child("startDate").getValue(String.class);
                    if (startDateStr != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date startDate = sdf.parse(startDateStr);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(startDate);
                            cal.add(Calendar.DAY_OF_MONTH, 28); // siklus default
                            String nextCycle = sdf.format(cal.getTime());

                            textCycle.setText("Menstruasi berikutnya: " + nextCycle);
                        } catch (Exception e) {
                            textCycle.setText("Data siklus tidak valid");
                        }
                    }
                } else {
                    textCycle.setText("Belum ada data siklus");
                }
            });

            // 🔹 Ambil mood dari Firestore
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

        return view;
    }

    private void generateCalendar() {
        calendarContainer.removeAllViews();

        // simpan kalender "hari ini"
        Calendar now = Calendar.getInstance();

        // bikin kalender untuk looping, dimundurin 3 hari
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);

        for (int i = -3; i <= 3; i++) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String dayName = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());

            // Container hari
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

            // Cek kalau tanggal, bulan, tahun sama dengan hari ini → tandai
            if (calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) &&
                    calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                    calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                dayBox.setBackgroundResource(R.drawable.bg_today);
            } else {
                dayBox.setBackgroundResource(R.drawable.bg_normal_day);
            }

            // Angka tanggal
            TextView dayNumber = new TextView(getContext());
            dayNumber.setText(String.valueOf(day));
            dayNumber.setTextSize(16);
            dayNumber.setTextColor(Color.parseColor("#6A0D4A"));
            dayNumber.setGravity(Gravity.CENTER);

            // Nama hari
            TextView dayText = new TextView(getContext());
            dayText.setText(dayName);
            dayText.setTextSize(10);
            dayText.setTextColor(Color.parseColor("#B0AFAF"));
            dayText.setGravity(Gravity.CENTER);

            // Tambahin ke box
            dayBox.addView(dayNumber);
            dayBox.addView(dayText);

            // Tambahin ke container
            calendarContainer.addView(dayBox);

            // lanjut ke tanggal berikutnya
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

}
