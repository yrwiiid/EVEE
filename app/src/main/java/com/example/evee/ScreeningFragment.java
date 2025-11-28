package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScreeningFragment extends Fragment {

    private int index = 0;

    private String[] questions = {
            "Berapa usia anda saat ini?",
            "Kapan hari terakhir menstruasi anda?",
            "Biasanya, berapa lama menstruasi anda berlangsung setiap bulan?",
            "Biasanya, berapa panjang siklus mesnstruasi anda?",
            "Apakah siklus menstruasi anda teratur?"
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

            // Pastikan user memilih jawaban
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
                Toast.makeText(getContext(), "Skrining selesai!", Toast.LENGTH_LONG).show();

                // Pindah ke Dashboard
                Intent intent = new Intent(getActivity(), dashboard.class);
                startActivity(intent);

                // Tutup activity screening agar tidak kembali lagi
                getActivity().finish();
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
}
