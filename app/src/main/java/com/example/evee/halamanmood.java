package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class halamanmood extends Fragment {

    private TextView dateText;
    private LinearLayout moodProgressContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halamanmood, container, false);

        moodProgressContainer = view.findViewById(R.id.moodProgressContainer);
        dateText = view.findViewById(R.id.dateText);

        String currentDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        dateText.setText(currentDate);

        loadTodayMood();

        return view;
    }

    private void loadTodayMood() {
        // Yg ini data dummy soale tanpa firestore
        ArrayList<String> listMood = new ArrayList<>();
        listMood.add("Senang");
        listMood.add("Sedih");
        listMood.add("Marah");
        listMood.add("Semangat");
        listMood.add("Bosan");
        listMood.add("Bingung");
        listMood.add("Percaya Diri");
        listMood.add("Manja");
        listMood.add("Ngantuk");
        listMood.add("Cemas");


        showMoodProgress(listMood);
    }

    private void showMoodProgress(ArrayList<String> listMood) {
        moodProgressContainer.removeAllViews();

        HashMap<String, Integer> counter = new HashMap<>();
        for (String m : listMood) counter.put(m, counter.getOrDefault(m, 0) + 1);

        int total = listMood.size();

        ArrayList<Map.Entry<String, Integer>> sortedList = new ArrayList<>(counter.entrySet());

        Collections.sort(sortedList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue(); // terbesar -> terkecil
            }
        });

        for (Map.Entry<String, Integer> entry : sortedList) {
            String mood = entry.getKey();
            int count = entry.getValue();
            int percent = Math.round((count * 100f) / total);

            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_mood_progress, moodProgressContainer, false);

            ImageView emojiView = item.findViewById(R.id.imgMood);
            TextView labelView = item.findViewById(R.id.txtMoodTitle);
            ProgressBar progressBar = item.findViewById(R.id.progressMood);
            TextView percentView = item.findViewById(R.id.txtPercent);

            emojiView.setImageResource(getEmojiDrawable(mood));
            labelView.setText(mood);
            progressBar.setProgress(percent);
            percentView.setText(percent + "%");

            moodProgressContainer.addView(item);
        }
    }

    private int getEmojiDrawable(String mood) {
        switch (mood) {
            case "Marah": return R.drawable.marah;
            case "Sedih": return R.drawable.sedih;
            case "Bingung": return R.drawable.bingung;
            case "Semangat": return R.drawable.semangat;
            case "Senang": return R.drawable.senang12;
            case "Bosan": return R.drawable.bosan;
            case "Percaya diri": return R.drawable.percayadiri;
            case "Manja": return R.drawable.senang11;
            case "Ngantuk": return R.drawable.senang10;
            case "Cemas": return R.drawable.cemas;
            default: return R.drawable.senang; // icon cadangan
        }
    }
}
