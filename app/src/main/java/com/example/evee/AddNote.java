package com.example.evee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddNote extends Fragment {

    LinearLayout layoutMiniCalendar;
    TextView txtSelectedDate;
    EditText edtNote;
    Button btnSaveNote;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    Calendar selectedDateCalendar;
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_note, container, false);

        layoutMiniCalendar = view.findViewById(R.id.layoutMiniCalendar);
        txtSelectedDate = view.findViewById(R.id.txtSelectedDate);
        edtNote = view.findViewById(R.id.edtNote);
        btnSaveNote = view.findViewById(R.id.btnSaveNote);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====> SET TANGGAL DEFAULT KE HARI INI <=====
        selectedDateCalendar = Calendar.getInstance();
        txtSelectedDate.setText(
                new SimpleDateFormat("dd MMMM yyyy", new Locale("id"))
                        .format(selectedDateCalendar.getTime())
        );

        generateMiniCalendar(inflater);

        btnSaveNote.setOnClickListener(v -> saveNote());

        return view;
    }

    private void generateMiniCalendar(LayoutInflater inflater) {
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Calendar itemDate = (Calendar) calendar.clone();

            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(60), dp(60));
            params.setMargins(0,0,dp(10),0);
            item.setLayoutParams(params);
            item.setBackgroundResource(R.drawable.bg_calendar_normal);

            TextView txtDay = new TextView(requireContext());
            TextView txtDate = new TextView(requireContext());

            SimpleDateFormat dayFormat = new SimpleDateFormat("E", new Locale("id"));
            String day = dayFormat.format(itemDate.getTime()).substring(0,1).toUpperCase();
            int tgl = itemDate.get(Calendar.DAY_OF_MONTH);

            txtDay.setText(day);
            txtDate.setText(String.valueOf(tgl));

            txtDay.setTextColor(Color.BLACK);
            txtDate.setTextColor(Color.BLACK);
            txtDay.setTextSize(12);
            txtDate.setTextSize(16);
            txtDay.setGravity(Gravity.CENTER);
            txtDate.setGravity(Gravity.CENTER);

            item.addView(txtDay);
            item.addView(txtDate);

            // Tandai hari ini
            Calendar today = Calendar.getInstance();
            if(itemDate.get(Calendar.YEAR)==today.get(Calendar.YEAR) &&
                    itemDate.get(Calendar.MONTH)==today.get(Calendar.MONTH) &&
                    itemDate.get(Calendar.DAY_OF_MONTH)==today.get(Calendar.DAY_OF_MONTH)) {
                item.setBackgroundResource(R.drawable.bg_calendar_selected);
            }

            item.setOnClickListener(v -> {
                selectedDateCalendar = (Calendar) itemDate.clone();
                txtSelectedDate.setText(
                        new SimpleDateFormat("dd MMMM yyyy", new Locale("id"))
                                .format(selectedDateCalendar.getTime())
                );
                // Reset background semua item
                for (int j=0;j<layoutMiniCalendar.getChildCount();j++){
                    layoutMiniCalendar.getChildAt(j).setBackgroundResource(R.drawable.bg_calendar_normal);
                }
                item.setBackgroundResource(R.drawable.bg_calendar_selected);
            });

            layoutMiniCalendar.addView(item);
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
    }

    private void saveNote(){
        if(selectedDateCalendar==null){
            Toast.makeText(requireContext(), "Pilih tanggal terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

        String noteText = edtNote.getText().toString().trim();
        if(noteText.isEmpty()){
            Toast.makeText(requireContext(), "Catatan kosong.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null) return;

        String uid = user.getUid();
        String dateStr = SDF.format(selectedDateCalendar.getTime());

        db.collection("Users").document(uid).collection("notes")
                .document(dateStr)
                .set(new NoteModel(dateStr,noteText), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Catatan tersimpan ✅", Toast.LENGTH_SHORT).show();
                    edtNote.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Gagal simpan: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Model note
    public static class NoteModel{
        String date;
        String note;
        public NoteModel(){}
        public NoteModel(String date, String note){
            this.date=date;
            this.note=note;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
