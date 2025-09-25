package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class halamanmood extends Fragment {

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halamanmood, container, false);

        Button btnHappy = view.findViewById(R.id.btnHappy);
        Button btnNeutral = view.findViewById(R.id.btnNeutral);
        Button btnSad = view.findViewById(R.id.btnSad);
        TextView txtMoodResult = view.findViewById(R.id.txtMoodResult);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        btnHappy.setOnClickListener(v -> saveMood("Senang 😊", txtMoodResult));
        btnNeutral.setOnClickListener(v -> saveMood("Biasa saja 😐", txtMoodResult));
        btnSad.setOnClickListener(v -> saveMood("Sedih 😢", txtMoodResult));

        return view;
    }

    private void saveMood(String mood, TextView txtMoodResult) {
        String uid = mAuth.getCurrentUser().getUid();

        dbRef.child(uid).child("mood").setValue(mood)
                .addOnSuccessListener(aVoid -> {
                    txtMoodResult.setText("Mood kamu: " + mood);
                    Toast.makeText(getActivity(), "Mood berhasil disimpan", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Gagal simpan mood", Toast.LENGTH_SHORT).show();
                });
    }
}
