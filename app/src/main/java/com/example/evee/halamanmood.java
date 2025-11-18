package com.example.evee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class halamanmood extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private HorizontalScrollView emojiScroll;
    private LinearLayout emojiContainer;
    private TextView moodText, dateText;
    private Button btnSimpan;
    private MoodBarChartView barChartView;

    private int selectedIndex = 4;
    private String selectedMood = "Senang";

    private final String[] emojis = {"😡", "😞", "😐", "🙂", "😊", "😃", "😆", "🤩", "😍", "😘", "😴", "😭"};
    private final String[] moods = {"Marah", "Sedih", "Biasa", "Cukup Senang", "Senang", "Bahagia", "Lucu", "Excited", "Cinta", "Manja", "Ngantuk", "Sedih Banget"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halamanmood, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emojiScroll = view.findViewById(R.id.emojiScroll);
        emojiContainer = view.findViewById(R.id.emojiContainer);
        moodText = view.findViewById(R.id.moodText);
        dateText = view.findViewById(R.id.dateText);
        btnSimpan = view.findViewById(R.id.btnSimpan);
        barChartView = view.findViewById(R.id.barChartView);

        // Tampilkan tanggal
        String currentDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        dateText.setText(currentDate);

        // Tambahkan emoji scroll
        for (String e : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(e);
            tv.setTextSize(32);
            tv.setTextColor(Color.parseColor("#55000000"));
            tv.setPadding(30, 0, 30, 0);
            tv.setShadowLayer(12f, 0f, 6f, Color.argb(80,0,0,0));
            tv.setElevation(10f);
            tv.setLetterSpacing(0.05f);
            tv.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            emojiContainer.addView(tv);
        }

        emojiScroll.getViewTreeObserver().addOnScrollChangedListener(this::updateEmojiSizes);
        emojiScroll.post(this::updateEmojiSizes);

        btnSimpan.setOnClickListener(v -> saveMood(selectedMood));

        loadTodayMood();

        // Grafik 6 mood terakhir user + emoji
        float[] dataValues = {1, 4, 3, 6, 8, 5}; // skala 1-10
        String[] dataLabels = {"Marah","Sedih","Biasa","Senang","Bahagia","Excited"};
        String[] dataEmojis = {"😡","😞","😐","😊","😃","🤩"};

        barChartView.setData(dataValues, dataLabels, dataEmojis);

        return view;
    }

    private void updateEmojiSizes() {
        int scrollX = emojiScroll.getScrollX();
        int centerX = emojiScroll.getWidth()/2;

        int closestIndex=0;
        int closestDistance=Integer.MAX_VALUE;

        for(int i=0;i<emojiContainer.getChildCount();i++){
            TextView v=(TextView)emojiContainer.getChildAt(i);
            int viewCenter=(v.getLeft()+v.getRight())/2;
            int distance=Math.abs(centerX+scrollX-viewCenter);

            float normalized=Math.min(distance,1000)/1000f;
            float scale=(float)Math.pow(1-normalized,2.8);

            float size=35+(scale*55);
            int alpha=(int)(60+(scale*195));

            float curveY=(float)Math.sin(normalized*Math.PI)*80;
            float shadow=12f*(1+scale);

            v.setShadowLayer(shadow,0f,6f,Color.argb((int)(100*scale),0,0,0));
            v.setTranslationZ(scale*10);
            v.setTextSize(size);
            v.setTextColor(Color.argb(alpha,0,0,0));
            v.setTranslationY(curveY);

            if(distance<closestDistance){
                closestDistance=distance;
                closestIndex=i;
            }
        }

        selectedIndex=closestIndex;
        selectedMood=moods[selectedIndex];
        moodText.setText(selectedMood);
    }

    private void saveMood(String mood){
        String uid=mAuth.getCurrentUser()!=null?mAuth.getCurrentUser().getUid():null;
        if(uid==null){
            Toast.makeText(getContext(),"User belum login",Toast.LENGTH_SHORT).show();
            return;
        }

        String today=new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(new Date());
        Map<String,Object> moodData=new HashMap<>();
        moodData.put("date",today);
        moodData.put("mood",mood);

        DocumentReference docRef=db.collection("Users").document(uid).collection("Mood").document(today);
        docRef.set(moodData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),"Mood disimpan: "+mood,Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),"Gagal menyimpan: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }

    private void loadTodayMood(){
        String uid=mAuth.getCurrentUser()!=null?mAuth.getCurrentUser().getUid():null;
        if(uid==null) return;

        String today=new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(new Date());
        DocumentReference docRef=db.collection("Users").document(uid).collection("Mood").document(today);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String mood=documentSnapshot.getString("mood");
                moodText.setText("Mood kamu: "+mood);
            }
        });
    }
}
