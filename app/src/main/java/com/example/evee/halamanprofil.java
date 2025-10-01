package com.example.evee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class halamanprofil extends Fragment {

    Button btnLogout;
    LinearLayout menuEditProfil, menuPengaturan, menuBantuan, menuTentang;

    public halamanprofil() {
        // Konstruktor kosong wajib untuk Fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate layout halaman_profil.xml
        View view = inflater.inflate(R.layout.halaman_profil, container, false);

        // Inisialisasi view
        btnLogout = view.findViewById(R.id.btnLogout);
        menuEditProfil = view.findViewById(R.id.menuEditProfil);
        menuPengaturan = view.findViewById(R.id.menuPengaturan);
        menuBantuan = view.findViewById(R.id.menuBantuan);
        menuTentang = view.findViewById(R.id.menuTentang);

        // Tombol logout
        btnLogout.setOnClickListener(v -> {
            // Hapus data login di SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            Toast.makeText(getActivity(), "Berhasil Logout", Toast.LENGTH_SHORT).show();

            // Arahkan ke LoginActivity
            Intent intent = new Intent(getActivity(), loginactivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Tutup activity dashboard
            requireActivity().finish();
        });

        // Klik menu
        menuEditProfil.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Edit Profil diklik", Toast.LENGTH_SHORT).show()
        );

        menuPengaturan.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Pengaturan diklik", Toast.LENGTH_SHORT).show()
        );

        menuBantuan.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Bantuan diklik", Toast.LENGTH_SHORT).show()
        );

        menuTentang.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Tentang Aplikasi diklik", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}
