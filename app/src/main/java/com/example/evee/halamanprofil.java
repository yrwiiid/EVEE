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

public class halamanprofil extends Fragment {

    private static final String TAG = "HalamanProfil";

    private Button btnEditProfile, btnLogout;
    private LinearLayout menuNotification, menuSiklus, menuTransfer, menuSettings;
    private TextView tvNama, tvUsername;
    private ImageView imgProfile;

    public halamanprofil() {
        // Konstruktor kosong wajib
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
        menuSettings = view.findViewById(R.id.menuSettings);

        // Load data user dari sharedprefs / server MySQL
        loadUserDataFromServer();

        // Tombol Edit Profil
        btnEditProfile.setOnClickListener(v -> {
            halamaneditprofil editProfilFragment = new halamaneditprofil();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, editProfilFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Tombol Logout
        btnLogout.setOnClickListener(v -> doLogout());

        // Menu lainnya
        menuNotification.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Menu Notification diklik", Toast.LENGTH_SHORT).show());

        menuSiklus.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Menu Siklus diklik", Toast.LENGTH_SHORT).show());

        menuTransfer.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Menu Transfer diklik", Toast.LENGTH_SHORT).show());

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

    // ====================================
    //   GANTI DENGAN MYSQL NANTI
    // ====================================
    private void loadUserDataFromServer() {
        // NANTI ambil dari server MySQL via HTTP
        // Contoh ambil dari SharedPreferences sementara

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        String nama = prefs.getString("nama", "Nama User");
        String username = prefs.getString("username", "username");

        tvNama.setText(nama);
        tvUsername.setText(username);

        Log.d(TAG, "Data user di-load (local only)");
    }

    // Logout hanya hapus sharedprefs
    private void doLogout() {
        SharedPreferences prefs =
                requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        prefs.edit().clear().apply();

        Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), loginactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
