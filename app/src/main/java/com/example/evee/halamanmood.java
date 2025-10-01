package com.example.evee;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class halamanmood extends Fragment {

    private Button btnHappy, btnNeutral, btnSad;
    private TextView txtMoodResult;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halamanmood, container, false);

        // Inisialisasi view
        btnHappy = view.findViewById(R.id.btnHappy);
        btnNeutral = view.findViewById(R.id.btnNeutral);
        btnSad = view.findViewById(R.id.btnSad);
        txtMoodResult = view.findViewById(R.id.txtMoodResult);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Listener tombol
        btnHappy.setOnClickListener(v -> saveMood("Senang 😊"));
        btnNeutral.setOnClickListener(v -> saveMood("Biasa 😐"));
        btnSad.setOnClickListener(v -> saveMood("Sedih 😢"));

        // Saat halaman dibuka → langsung cek mood hari ini
        loadTodayMood();

        return view;
    }

    private void saveMood(String mood) {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format tanggal hari ini
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Data disimpan ke Firestore
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("date", today);
        moodData.put("mood", mood);

        DocumentReference docRef = db.collection("Users")
                .document(uid)
                .collection("Mood")
                .document(today);

        docRef.set(moodData).addOnSuccessListener(aVoid -> {
            txtMoodResult.setText("Mood kamu: " + mood);
            Toast.makeText(getContext(), "Mood tersimpan", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadTodayMood() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        DocumentReference docRef = db.collection("Users")
                .document(uid)
                .collection("Mood")
                .document(today);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String mood = documentSnapshot.getString("mood");
                txtMoodResult.setText("Mood kamu: " + mood);
            }
        });
    }
}
