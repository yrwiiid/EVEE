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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class halamaneditprofil extends Fragment {

    private EditText etNama, etEmail, etUsername;
    private Button btnSimpan;

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

        SessionManager sessionManager = new SessionManager(requireActivity());
        String userId = sessionManager.getUserId();

        try {
            JSONObject body = new JSONObject();
            body.put("id", userId);
            body.put("name", nama);
            body.put("email", email);
            body.put("username", username);

            String url = ApiConfig.BASE_URL + "users.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(requireContext(), "Data berhasil disimpan ke server!", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    },
                    error -> {
                        Toast.makeText(requireContext(), "Gagal simpan data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            );

            Volley.newRequestQueue(requireContext()).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Terjadi kesalahan saat menyiapkan data", Toast.LENGTH_SHORT).show();
        }
    }
}

