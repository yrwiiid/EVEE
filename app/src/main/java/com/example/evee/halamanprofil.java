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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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
        SessionManager sessionManager = new SessionManager(requireActivity());
        String userId = sessionManager.getUserId();

        String url = ApiConfig.BASE_URL + "users.php?id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject userObj = response.getJSONObject("user");

                        String nama = userObj.getString("name");
                        String email = userObj.getString("email"); // ini sebagai "username"

                        tvNama.setText(nama);
                        tvUsername.setText(email);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Gagal parsing data user", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("HalamanProfil", "Gagal ambil data user: " + error.getMessage());
                    Toast.makeText(requireContext(), "Gagal ambil data user", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }


    // Logout hanya hapus sharedprefs
    private void doLogout() {

        SessionManager session = new SessionManager(requireActivity());
        session.clearSession();

        Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), loginactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
