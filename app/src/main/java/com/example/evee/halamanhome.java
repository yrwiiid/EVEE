package com.example.evee;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class halamanhome extends Fragment implements MoodUpdatePopup.OnMoodSavedListener {

    private TextView textGreeting, textDate, textCycle, textCycleStatus, textMoodDesc, textMoodComment;
    private ImageView imageMoodIcon;
    private CardView cardMood;
    private Button buttonEditMood;
    private Button buttonLogPeriod;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    private final int[] moodImages = {
            R.drawable.bingung, R.drawable.semangat, R.drawable.cemas, R.drawable.lelah,
            R.drawable.marah, R.drawable.senang, R.drawable.sedih, R.drawable.percayadiri,
            R.drawable.bosan, R.drawable.senang10, R.drawable.senang11, R.drawable.senang12
    };

    private final String[] labels = {
            "Bingung","Semangat","Cemas","Lelah","Marah","Senang",
            "Sedih","Percaya Diri","Bosan","Manja","Ngantuk","Sedih Banget"
    };

    private final String[] moodComments = {
            "Waduh, jangan bingung ya, tarik napas dulu~",
            "Ayo semangat! Kamu pasti bisa!",
            "Tenang, cemasnya hilang kalau kita senyum :)",
            "Lelah ya? Yuk istirahat sebentar~",
            "Ups, marah-marah nggak asik, yuk tarik napas!",
            "Yeay, senangnya ketemu hari yang ceria!",
            "Sedih ya? Peluk hangat buatmu 💛",
            "Percaya diri dong! Kamu hebat kok!",
            "Bosan? Yuk cari hal seru!",
            "Manja boleh, tapi jangan lupa senyum ya~",
            "Ngantuk? Tidur sebentar biar segar lagi!",
            "Sedih banget? Tenang, semuanya akan baik-baik saja!"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halaman_home, container, false);

        // INISIALISASI VIEW
        textGreeting = view.findViewById(R.id.textGreeting);
        textDate = view.findViewById(R.id.textDate);
        textCycle = view.findViewById(R.id.textCycle);
        textCycleStatus = view.findViewById(R.id.textCycleStatus);
        textMoodDesc = view.findViewById(R.id.textMoodDesc);
        textMoodComment = view.findViewById(R.id.textMoodComment);
        imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
        cardMood = view.findViewById(R.id.cardMood);
        buttonEditMood = view.findViewById(R.id.buttonEditMood);
        buttonLogPeriod = view.findViewById(R.id.buttonLogPeriod);

        // GREETING DEFAULT
        textGreeting.setText("Halo pengguna!");

        // SET TANGGAL
        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        textDate.setText(todayDate);

        // DATA SIKLUS (placeholder)
        textCycle.setText("—");
        textCycleStatus.setText("Belum ada data siklus");

        // MOOD DEFAULT
        textMoodDesc.setText("Belum ada mood");
        textMoodComment.setText("Yuk, catat mood-mu hari ini!");
        imageMoodIcon.setVisibility(View.GONE);

        // TOMBOL BUKA KALENDER
        if (buttonLogPeriod != null) {
            buttonLogPeriod.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, new halamancalendar())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // POPUP EDIT MOOD
        if (buttonEditMood != null) {
            buttonEditMood.setOnClickListener(v -> {
                MoodUpdatePopup popup = new MoodUpdatePopup(getContext(), (moodLabel, moodEmoji) -> {
                    updateMoodUI(moodLabel);
                    Toast.makeText(getContext(), "Mood disimpan (lokal)", Toast.LENGTH_SHORT).show();
                });
                popup.show();
            });
        }

        // POPUP OTOMATIS (dummy)
        checkMoodTodayPopup();

        return view;
    }

    // SIMULASI POPUP OTOMATIS
    private void checkMoodTodayPopup() {
        new Handler().postDelayed(() -> {
            MoodPopup popup = new MoodPopup(getContext(), (moodLabel, moodEmoji) -> {
                updateMoodUI(moodLabel);
            });
            popup.show();
        }, 300);
    }

    // UPDATE UI MOOD
    private void updateMoodUI(String moodLabel) {
        textMoodDesc.setText(moodLabel);
        textMoodComment.setText(getCommentForLabel(moodLabel));

        int resId = getDrawableForLabel(moodLabel);
        if (resId != 0) {
            imageMoodIcon.setImageResource(resId);
            imageMoodIcon.setVisibility(View.VISIBLE);
        } else {
            imageMoodIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMoodSaved(String moodLabel, String moodEmoji) {
        updateMoodUI(moodLabel);
    }

    private int getDrawableForLabel(String label) {
        if (label == null) return 0;
        for (int i = 0; i < labels.length; i++) {
            if (label.equals(labels[i])) return moodImages[i];
        }
        return 0;
    }

    private String getCommentForLabel(String label) {
        if (label == null) return "Yuk, catat mood-mu hari ini!";
        for (int i = 0; i < labels.length; i++) {
            if (label.equals(labels[i])) return moodComments[i];
        }
        return "Tetap semangat ya!";
    }
}
