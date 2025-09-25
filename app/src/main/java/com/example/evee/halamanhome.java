package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class halamanhome extends Fragment {

    private TextView textGreeting, textDate, textCycle, textMood;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halaman_home, container, false);

        textGreeting = view.findViewById(R.id.textGreeting);
        textDate = view.findViewById(R.id.textDate);
        textCycle = view.findViewById(R.id.textCycle);
        textMood = view.findViewById(R.id.textMood);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        // 🔹 Salam pakai nama user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            textGreeting.setText(name != null && !name.isEmpty()
                    ? "Halo, " + name + "!"
                    : "Halo, pengguna!");
        }

        // 🔹 Tampilkan tanggal hari ini
        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        textDate.setText(todayDate);

        // 🔹 Ambil data siklus dari Firebase
        if (currentUser != null) {
            String uid = currentUser.getUid();
            dbRef.child(uid).child("cycle").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String startDateStr = snapshot.child("startDate").getValue(String.class);
                    String endDateStr = snapshot.child("endDate").getValue(String.class);

                    if (startDateStr != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date startDate = sdf.parse(startDateStr);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(startDate);
                            cal.add(Calendar.DAY_OF_MONTH, 28); // default 28 hari
                            String nextCycle = sdf.format(cal.getTime());

                            textCycle.setText("Prediksi haid berikutnya: " + nextCycle);
                        } catch (Exception e) {
                            textCycle.setText("Data siklus tidak valid");
                        }
                    }

                    if (endDateStr != null) {
                        textMood.setText("Haid terakhir: " + endDateStr);
                    }
                } else {
                    textCycle.setText("Belum ada data siklus");
                    textMood.setText("-");
                }
            });
        }

        return view;
    }
}
