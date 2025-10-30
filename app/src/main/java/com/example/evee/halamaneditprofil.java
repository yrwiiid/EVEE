package com.example.evee;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class halamaneditprofil extends Fragment {

    private EditText etNama, etEmail, etUsername;
    private Button btnSimpan;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    public halamaneditprofil() {
        // konstruktor kosong wajib
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile, container, false);

        etNama = view.findViewById(R.id.etNama);
        etEmail = view.findViewById(R.id.etEmail);
        etUsername = view.findViewById(R.id.etUsername);
        btnSimpan = view.findViewById(R.id.btnSimpan);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        btnSimpan.setOnClickListener(v -> simpanData());

        return view;
    }

    private void simpanData() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (TextUtils.isEmpty(nama) || TextUtils.isEmpty(email) || TextUtils.isEmpty(username)) {
            Toast.makeText(requireContext(), "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            dbRef.child(uid).child("name").setValue(nama);
            dbRef.child(uid).child("email").setValue(email);
            dbRef.child(uid).child("username").setValue(username);

            Toast.makeText(requireContext(), "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show();

            // Kembali ke halaman profil
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "User tidak ditemukan!", Toast.LENGTH_SHORT).show();
        }
    }
}
