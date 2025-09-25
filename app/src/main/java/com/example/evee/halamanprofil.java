package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class halamanprofil extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout dengan false supaya tidak auto-attach ke container
        View view = inflater.inflate(R.layout.halaman_profil, container, false);

        // Ambil tombol logout dari layout
        MaterialButton buttonLogout = view.findViewById(R.id.buttonLogout);

        // Aksi saat tombol diklik
        buttonLogout.setOnClickListener(v -> {
            // Hapus session SharedPreferences
            requireActivity()
                    .getSharedPreferences("USER_SESSION", requireActivity().MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            // Sign out dari FirebaseAuth
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

            // Intent ke LoginActivity dan clear semua back stack
            Intent intent = new Intent(getActivity(), loginactivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // Tutup activity utama supaya tidak bisa kembali
            requireActivity().finish();
        });


        return view;
    }
}
