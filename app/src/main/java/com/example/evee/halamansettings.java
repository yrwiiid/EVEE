package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;


public class halamansettings extends Fragment {

    private LinearLayout menuChangePassword, menuHelp, menuAbout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halaman_settings, container, false);

        // Inisialisasi komponen
        menuChangePassword = view.findViewById(R.id.menuChangePassword);
        menuHelp = view.findViewById(R.id.menuHelp);
        menuAbout = view.findViewById(R.id.menuAbout);

        // Klik "Ganti Password"
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
            startActivity(intent);
        });


        // Klik "Bantuan"
        menuHelp.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Buka halaman Bantuan", Toast.LENGTH_SHORT).show();
            // Di sini nanti bisa buka activity Bantuan atau fragment baru
        });

        // Klik "Tentang Aplikasi"
        menuAbout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Buka halaman Tentang Aplikasi", Toast.LENGTH_SHORT).show();
            // Nanti bisa tampilkan informasi aplikasi
        });

        return view;
    }
}
