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

import android.view.ViewGroup;
import com.bumptech.glide.Glide;

public class MoodUpdatePopup extends Dialog {

    private final Context context;
    private final OnMoodSavedListener listener;

    private String selectedMoodId   = null;
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

        // lebar dialog biar enak
        if (getWindow() != null) {
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85);
            getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        GridLayout gridMoodIcons   = findViewById(R.id.gridMoodIcons);
        TextView moodSelectedText  = findViewById(R.id.moodSelectedText);
        Button btnSaveMood         = findViewById(R.id.btnSaveMood);
        ImageView btnClose         = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> dismiss());

        String url = ApiConfig.MOODS_URL;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        gridMoodIcons.removeAllViews();
                        gridMoodIcons.setColumnCount(3);

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject moodObj = response.getJSONObject(i);
                            String moodId   = moodObj.getString("id");
                            String moodName = moodObj.getString("name");
                            String iconFile = moodObj.optString("icon", "");

                            // item wrapper
                            LinearLayout item = new LinearLayout(context);
                            item.setOrientation(LinearLayout.VERTICAL);
                            item.setGravity(Gravity.CENTER);

                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 0;
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                            params.setMargins(8, 8, 8, 8);
                            item.setLayoutParams(params);

                            // icon
                            ImageView icon = new ImageView(context);
                            int sizeDp = 72;
                            int sizePx = (int) (sizeDp *
                                    context.getResources().getDisplayMetrics().density);
                            LinearLayout.LayoutParams lp =
                                    new LinearLayout.LayoutParams(sizePx, sizePx);
                            icon.setLayoutParams(lp);
                            icon.setAdjustViewBounds(true);
                            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            int resId = getLocalMoodIcon(iconFile);
                            icon.setImageResource(resId);


                            // label
                            TextView label = new TextView(context);
                            label.setText(moodName);
                            label.setGravity(Gravity.CENTER);
                            label.setTextSize(13f);
                            label.setPadding(0, 4, 0, 0);

                            item.addView(icon);
                            item.addView(label);

                            item.setOnClickListener(v -> {
                                selectedMoodId   = moodId;
                                selectedMoodName = moodName;
                                selectedIconFile = iconFile;
                                moodSelectedText.setText("Mood dipilih: " + moodName);
                            });

                            gridMoodIcons.addView(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Gagal parse moods", Toast.LENGTH_SHORT).show();
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
    private int getLocalMoodIcon(String iconFileName) {
        if (iconFileName == null) {
            return R.drawable.mascnormal; // default
        }

        // buang ekstensi .png / .jpg
        String baseName = iconFileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = baseName.substring(0, dotIndex);
        }

        // cari resource drawable dengan nama itu
        int resId = context.getResources()
                .getIdentifier(baseName, "drawable", context.getPackageName());

        if (resId == 0) {
            resId = R.drawable.mascnormal; // fallback kalau tidak ketemu
        }
        return resId;
    }

}
