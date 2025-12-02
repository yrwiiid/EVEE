package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ScreeningFragment extends Fragment {

    private int index = 0;

    private String[] questions = {
            "Berapa usia anda saat ini?",
            "Kapan hari terakhir menstruasi anda?",
            "Biasanya, berapa lama menstruasi anda berlangsung setiap bulan?",
            "Biasanya, berapa panjang siklus mesnstruasi anda?",
            "Apakah siklus menstruasi anda teratur?",
            "Seberapa tingkat nyeri menstruasi yang Anda rasakan?"
    };

    private String[][] choices = {
            {"Dibawah 18 tahun", "18-24 tahun", "25-34 tqhun", "35-44 tahun", "45 tahun keatas"},
            {"Dalam 7 hari terakhir", "Sekitar 1–2 minggu yang lalu", "Sekitar 3–4 minggu yang lalu", "Saya tidak ingat pasti"},
            {"Kurang dari 3 hari", "3–5 hari", "6–7 hari", "Lebih dari 7 hari"},
            {"<25 hari", "25-30 hari ", "<30 hari", "Tidak pasti"},
            {"Teratur", "Kadang telat", "Sangat tidak teratur"},
            {"Nyeri ringan", "Nyeri sedang", "Nyeri berat"}
    };

    private int[] answers = new int[questions.length];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_screening, container, false);

        TextView txtQuestion = v.findViewById(R.id.txtQuestion);
        TextView txtProgress = v.findViewById(R.id.txtProgress);
        RadioGroup radioGroup = v.findViewById(R.id.radioGroup);
        Button btnNext = v.findViewById(R.id.btnNext);

        updateUI(txtQuestion, txtProgress, radioGroup);

        btnNext.setOnClickListener(view -> {

            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Pilih jawaban dulu ya", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedButton = v.findViewById(selectedId);
            answers[index] = radioGroup.indexOfChild(selectedButton);

            index++;

            if (index < questions.length) {
                updateUI(txtQuestion, txtProgress, radioGroup);
            } else {
                submitScreening();
            }
        });

        return v;
    }

    private void updateUI(TextView txtQuestion, TextView txtProgress, RadioGroup radioGroup) {
        txtQuestion.setText(questions[index]);
        txtProgress.setText((index + 1) + " / " + questions.length);

        radioGroup.removeAllViews();

        for (String choice : choices[index]) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(choice);
            rb.setTextSize(16);
            radioGroup.addView(rb);
        }
    }

    // ===========================================
    //   KIRIM HASIL SCREENING KE API
    // ===========================================
    private void submitScreening() {

        SessionManager session = new SessionManager(requireContext());
        String userId = session.getUserId();

        if (userId == null) {
            Toast.makeText(getContext(), "Session hilang! Silakan login ulang", Toast.LENGTH_SHORT).show();
            return;
        }


        String url = ApiConfig.SCREENING_URL;

        try {
            // Buat JSON
            JSONObject body = new JSONObject();
            body.put("user_id", userId);

            JSONArray arr = new JSONArray();
            for (int ans : answers) { arr.put(ans); }

            body.put("answers", arr);

            Log.e("SCREENING_DEBUG", "Body = " + body.toString());

            StringRequest request = new StringRequest(
                    Request.Method.POST, url,
                    response -> {
                        Log.e("SCREENING_DEBUG", "Response = " + response);

                        Toast.makeText(getContext(), "Screening tersimpan!", Toast.LENGTH_LONG).show();

                        // Pindah ke dashboard
                        Intent intent = new Intent(getActivity(), dashboard.class);
                        startActivity(intent);
                        requireActivity().finish();
                    },
                    error -> {
                        Toast.makeText(getContext(), "Gagal mengirim data!", Toast.LENGTH_SHORT).show();
                        Log.e("SCREENING_DEBUG", "Error = " + error.toString());
                    }
            ) {
                @Override
                public byte[] getBody() {
                    return body.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Volley.newRequestQueue(requireContext()).add(request);

        } catch (Exception e) {
            Log.e("SCREENING_DEBUG", "JSON ERROR = " + e.getMessage());
        }
    }
}
