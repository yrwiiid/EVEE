package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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
        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();

        String url = ApiConfig.BASE_URL + "mood_logs.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    ArrayList<String> listMood = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String moodName = obj.optString("mood_name", "Netral");
                            listMood.add(moodName); // cukup ambil mood_name
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showMoodProgress(listMood);
                },
                error -> Toast.makeText(requireContext(), "Gagal load mood: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
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
            String moodName = entry.getKey();
            int count = entry.getValue();
            int percent = Math.round((count * 100f) / total);

            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_mood_progress, moodProgressContainer, false);

            ImageView emojiView = item.findViewById(R.id.imgMood);
            TextView labelView = item.findViewById(R.id.txtMoodTitle);
            ProgressBar progressBar = item.findViewById(R.id.progressMood);
            TextView percentView = item.findViewById(R.id.txtPercent);

            labelView.setText(moodName);
            progressBar.setProgress(percent);
            percentView.setText(percent + "%");

            // pakai hardcode mapping
            int resId = getEmojiDrawable(moodName);
            emojiView.setImageResource(resId);

            moodProgressContainer.addView(item);
        }
    }

    // Hardcode mapping mood → drawable lokal
    private int getEmojiDrawable(String mood) {
        switch (mood) {
            case "Sensitif": return R.drawable.marah;
            case "Sedih": return R.drawable.sedih;
            case "Netral": return R.drawable.bingung;
            case "Capek": return R.drawable.lelah;
            case "Senang": return R.drawable.senang12;
            case "Cemas": return R.drawable.cemas;
            default: return R.drawable.normal; // fallback
        }
    }
}
