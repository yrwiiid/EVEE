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

    // View
    private Button btnLogout;
    private LinearLayout menuEditProfil, menuPengaturan, menuBantuan, menuTentang;
    private TextView tvName, tvPhone;
    private ImageView imgProfile;

    // Firebase
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

        // Init view
        btnLogout = view.findViewById(R.id.btnLogout);
        menuEditProfil = view.findViewById(R.id.menuEditProfil);
        menuPengaturan = view.findViewById(R.id.menuPengaturan);
        menuBantuan = view.findViewById(R.id.menuBantuan);
        menuTentang = view.findViewById(R.id.menuTentang);
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        imgProfile = view.findViewById(R.id.imgProfile);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://evee-aee6e-default-rtdb.firebaseio.com/");

        // Ambil data user aktif
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserData(user.getUid());
        } else {
            tvName.setText("User tidak login");
            tvPhone.setText("-");
        }

        // Tombol Logout
        btnLogout.setOnClickListener(v -> doLogout());

        // Menu klik
        menuEditProfil.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Edit Profil diklik", Toast.LENGTH_SHORT).show()
        );
        menuPengaturan.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Pengaturan diklik", Toast.LENGTH_SHORT).show()
        );
        menuBantuan.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Bantuan diklik", Toast.LENGTH_SHORT).show()
        );
        menuTentang.setOnClickListener(v ->
                Toast.makeText(requireActivity(), "Tentang Aplikasi diklik", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void loadUserData(String uid) {
        // Ambil dari Firebase Realtime Database (Users/UID)
        dbRef.child("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);

                            tvName.setText(name != null ? name : "Nama tidak tersedia");
                            tvPhone.setText(phone != null ? phone : (email != null ? email : "-"));
                        } else {
                            tvName.setText("Data tidak ditemukan");
                            tvPhone.setText("-");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Gagal ambil data: " + error.getMessage());
                    }
                });
    }

    private void doLogout() {
        // Hapus SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Logout Firebase
        mAuth.signOut();

        Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show();

        // Intent ke halaman login
        Intent intent = new Intent(requireActivity(), loginactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        requireActivity().startActivity(intent);

        requireActivity().finishAffinity();
    }
}
