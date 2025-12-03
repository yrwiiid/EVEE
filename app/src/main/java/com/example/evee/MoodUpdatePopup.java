package com.example.evee;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MoodUpdatePopup extends Dialog {

    private Context context;
    private OnMoodSavedListener listener;

    private String selectedMoodId = null;
    private String selectedMoodName = null;
    private String selectedIconFile = null;

    public interface OnMoodSavedListener {
        void onMoodSaved(String moodLabel, String moodId, String iconFile);
    }

    public MoodUpdatePopup(@NonNull Context context, OnMoodSavedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_mood);

        GridLayout gridMoodIcons = findViewById(R.id.gridMoodIcons);
        TextView moodSelectedText = findViewById(R.id.moodSelectedText);
        Button btnSaveMood = findViewById(R.id.btnSaveMood);
        ImageView btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> dismiss());

        String url = ApiConfig.MOODS_URL;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject moodObj = response.getJSONObject(i);
                            String moodId = moodObj.getString("id");
                            String moodName = moodObj.getString("name");
                            String iconFile = moodObj.getString("icon");

                            // Container untuk tiap item mood
                            LinearLayout item = new LinearLayout(context);
                            item.setOrientation(LinearLayout.VERTICAL);
                            item.setGravity(Gravity.CENTER);
                            item.setPadding(16, 16, 16, 16);

                            // Icon mood lebih besar
                            ImageView icon = new ImageView(context);
                            int sizeInDp = 120; // ukuran icon lebih besar
                            int sizeInPx = (int) (sizeInDp * context.getResources().getDisplayMetrics().density);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                            lp.setMargins(12, 12, 12, 12);
                            icon.setLayoutParams(lp);
                            icon.setAdjustViewBounds(true);
                            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            icon.setImageResource(getMoodIconRes(iconFile));

                            // Label mood
                            TextView label = new TextView(context);
                            label.setText(moodName);
                            label.setGravity(Gravity.CENTER);
                            label.setPadding(0, 8, 0, 0);

                            item.addView(icon);
                            item.addView(label);

                            item.setOnClickListener(v -> {
                                selectedMoodId = moodId;
                                selectedMoodName = moodName;
                                selectedIconFile = iconFile;
                                moodSelectedText.setText("Mood dipilih: " + moodName);
                            });

                            gridMoodIcons.addView(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context, "Gagal load moods", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(context).add(request);

        btnSaveMood.setOnClickListener(v -> {
            if (selectedMoodId != null) {
                listener.onMoodSaved(selectedMoodName, selectedMoodId, selectedIconFile);
                dismiss();
            } else {
                Toast.makeText(context, "Pilih mood dulu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getMoodIconRes(String iconFileName) {
        switch (iconFileName) {
            case "masc-senang.png": return R.drawable.senang;
            case "m002.png": return R.drawable.sedih;
            case "masc-cemas.png": return R.drawable.cemas;
            case "masc-capek.png": return R.drawable.lelah;
            case "m005.png": return R.drawable.bingung;
            case "masc-sensitif.png": return R.drawable.bosan;
            default: return R.drawable.percayadiri;
        }
    }
}
