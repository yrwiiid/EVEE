package com.example.evee;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoodUpdatePopup {

    public interface OnMoodSavedListener {
        void onMoodSaved(String moodLabel, String moodEmoji);
    }

    private final Context context;
    private final OnMoodSavedListener listener;
    private AlertDialog dialog;

    // Array untuk icon drawable (sesuaikan dengan nama file di drawable)
    private final int[] moodIcons = {
            R.drawable.senang1,  // Senang
            R.drawable.senang2,  // Senang
            R.drawable.senang3,  // Senang
            R.drawable.senang4,  // Senang
            R.drawable.senang5,  // Senang
            R.drawable.senang6,  // Senang
            R.drawable.senang7,  // Senang
            R.drawable.senang8,  // Senang
            R.drawable.senang9,  // Senang
            R.drawable.senang10, // Senang
            R.drawable.senang11, // Senang
            R.drawable.senang12  // Senang
    };

    private final String[] labels = {
            "Marah","Sedih","Biasa","Cukup Senang","Senang","Bahagia",
            "Lucu","Excited","Cinta","Manja","Ngantuk","Sedih Banget"
    };

    public MoodUpdatePopup(Context c, OnMoodSavedListener l) {
        this.context = c;
        this.listener = l;
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.popup_mood_update, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView btnClose = view.findViewById(R.id.btnClose);
        GridLayout gridMoodIcons = view.findViewById(R.id.gridMoodIcons);
        TextView moodText = view.findViewById(R.id.moodSelectedText);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Tambahkan semua mood icons ke grid
        for (int i = 0; i < moodIcons.length; i++) {
            LinearLayout itemLayout = createMoodItem(i, moodText);
            gridMoodIcons.addView(itemLayout);
        }

        dialog.show();
    }

    private LinearLayout createMoodItem(int index, TextView moodText) {
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

        // Click listener - LANGSUNG SIMPAN DAN TUTUP
        layout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoodSaved(labels[index], "😊");
            }
            dialog.dismiss();
        });

        return layout;
    }
}