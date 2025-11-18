package com.example.evee;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoodPopup {

    public interface OnMoodSavedListener {
        void onMoodSaved(String moodLabel, String moodEmoji);
    }

    private final Context context;
    private final OnMoodSavedListener listener;
    private AlertDialog dialog;

    // Array untuk icon drawable (sesuaikan dengan nama file di drawable)
    private final int[] moodIcons = {
            R.drawable.bingung,
            R.drawable.semangat,
            R.drawable.cemas,
            R.drawable.lelah,
            R.drawable.marah,
            R.drawable.senang,
            R.drawable.sedih,
            R.drawable.percayadiri,
            R.drawable.bosan,
            R.drawable.senang10, // Senang
            R.drawable.senang11, // Senang
            R.drawable.senang12  // Senang
    };

    private final String[] labels = {
            "Bingung", "Semangat", "Cemas",
            "Lemas", "Marah", "Senang",
            "Sedih", "Percaya Diri", "Bosan",
            "Senang", "Senang", "Senang"
    };

    private int selectedIndex = -1;

    public MoodPopup(Context c, OnMoodSavedListener l) {
        this.context = c;
        this.listener = l;
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.popup_mood, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView btnClose = view.findViewById(R.id.btnClose);
        GridLayout gridMoodIcons = view.findViewById(R.id.gridMoodIcons);
        TextView moodText = view.findViewById(R.id.moodSelectedText);
        Button saveBtn = view.findViewById(R.id.btnSaveMood);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Tambahkan semua mood icons ke grid
        for (int i = 0; i < moodIcons.length; i++) {
            LinearLayout itemLayout = createMoodItem(i, gridMoodIcons, moodText);
            gridMoodIcons.addView(itemLayout);
        }

        saveBtn.setOnClickListener(v -> {
            if (selectedIndex >= 0 && listener != null) {
                listener.onMoodSaved(labels[selectedIndex], "😊");
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private LinearLayout createMoodItem(int index, GridLayout grid, TextView moodText) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(10, 10, 10, 10);

        // Set ukuran untuk setiap item
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        layout.setLayoutParams(params);

        // ImageView untuk icon mood
        ImageView icon = new ImageView(context);
        icon.setImageResource(moodIcons[index]);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                (int) (80 * context.getResources().getDisplayMetrics().density),
                (int) (80 * context.getResources().getDisplayMetrics().density)
        );
        icon.setLayoutParams(iconParams);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // TextView untuk label
        TextView label = new TextView(context);
        label.setText(labels[index]);
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 8, 0, 0);

        layout.addView(icon);
        layout.addView(label);

        // Click listener
        layout.setOnClickListener(v -> {
            selectedIndex = index;
            moodText.setText(labels[index]);

            // Update visual selection
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                child.setBackgroundResource(0);
            }
            layout.setBackgroundResource(R.drawable.bg_mood_selected);
        });

        return layout;
    }
}