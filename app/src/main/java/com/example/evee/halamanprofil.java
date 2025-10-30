package com.example.evee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class halamanprofil extends Fragment {

    private static final String TAG = "HalamanProfil";

    private Button btnEditProfile, btnLogout;
    private LinearLayout menuNotification, menuSiklus, menuTransfer, menuSettings; // ✅ tambahkan menuSettings
    private TextView tvNama, tvUsername;
    private ImageView imgProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    public halamanprofil() {
        // Konstruktor kosong wajib untuk Fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halaman_profil, container, false);

        // Inisialisasi View
        tvNama = view.findViewById(R.id.tvNama);
        tvUsername = view.findViewById(R.id.tvUsername);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        menuNotification = view.findViewById(R.id.menuNotification);
        menuSiklus = view.findViewById(R.id.menuSiklus);
        menuTransfer = view.findViewById(R.id.menuTransfer);
        menuSettings = view.findViewById(R.id.menuSettings); // ✅ inisialisasi pengaturan

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        // Ambil data user aktif
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserData(user.getUid());
        } else {
            tvNama.setText("User tidak login");
            tvUsername.setText("-");
        }

        // 🔹 Edit Profil -> Ganti fragment
        btnEditProfile.setOnClickListener(v -> {
            halamaneditprofil editProfilFragment = new halamaneditprofil();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, editProfilFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 🔹 Logout
        btnLogout.setOnClickListener(v -> doLogout());

        // 🔹 Menu Notification
        menuNotification.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Menu Notification diklik", Toast.LENGTH_SHORT).show());

        // 🔹 Menu Siklus
        menuSiklus.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Menu Siklus diklik", Toast.LENGTH_SHORT).show());

        // 🔹 Menu Transfer
        menuTransfer.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Menu Transfer diklik", Toast.LENGTH_SHORT).show());

        // 🔹 Menu Pengaturan (buka halaman pengaturan)
        menuSettings.setOnClickListener(v -> {
            halamansettings settingsFragment = new halamansettings();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, settingsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadUserData(String uid) {
        dbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nama = snapshot.child("name").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    tvNama.setText(nama != null ? nama : "Nama tidak tersedia");
                    tvUsername.setText(username != null ? username : "-");
                } else {
                    tvNama.setText("Data tidak ditemukan");
                    tvUsername.setText("-");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Gagal ambil data: " + error.getMessage());
            }
        });
    }

    private void doLogout() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        mAuth.signOut();

        Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), loginactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
