package com.example.evee;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class halamancalendar extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView textSelectedRange;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private CalendarDay startDate = null;
    private CalendarDay endDate = null;

    private final List<CalendarDay> selectedRange = new ArrayList<>();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halamancalendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        textSelectedRange = view.findViewById(R.id.textSelectedRange);
        btnSave = view.findViewById(R.id.btnSavePeriod);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        // restore siklus sebelumnya
        restoreSavedCycle();

        // klik tanggal
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;

            if (startDate == null) {
                // klik pertama = start
                startDate = date;
                endDate = null;
                selectedRange.clear();
                calendarView.removeDecorators();
                textSelectedRange.setText("Mulai: " + formatDate(startDate));
            } else {
                // klik kedua = end
                endDate = date;

                // ✅ FIX: kalau tanggal sama → 1 hari saja
                if (startDate.equals(endDate)) {
                    selectedRange.clear();
                    selectedRange.add(startDate);

                    textSelectedRange.setText("Haid: " + formatDate(startDate));
                    calendarView.removeDecorators();
                    calendarView.addDecorator(new RangeCircleDecorator(selectedRange, requireContext()));

                    startDate = null;
                    endDate = null;
                    return; // stop di sini
                }

                // pastikan urutan start <= end
                Calendar startCal = (Calendar) startDate.getDate().clone();
                Calendar endCal = (Calendar) endDate.getDate().clone();
                if (startCal.after(endCal)) {
                    CalendarDay tmp = startDate;
                    startDate = endDate;
                    endDate = tmp;

                    startCal = (Calendar) startDate.getDate().clone();
                    endCal = (Calendar) endDate.getDate().clone();
                }

                // build range
                selectedRange.clear();
                Calendar iter = (Calendar) startCal.clone();
                while (!iter.after(endCal)) {
                    selectedRange.add(CalendarDay.from(iter));
                    iter.add(Calendar.DAY_OF_MONTH, 1);
                }

                textSelectedRange.setText("Haid: " + formatDate(startDate) + " - " + formatDate(endDate));
                calendarView.removeDecorators();
                calendarView.addDecorator(new RangeCircleDecorator(selectedRange, requireContext()));

                startDate = null;
                endDate = null;
            }
        });

        // tombol simpan
        btnSave.setOnClickListener(v -> {
            if (selectedRange.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih rentang tanggal dulu (klik 2 tanggal).", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(requireContext(), "User belum login.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();
            String start = formatDate(selectedRange.get(0));
            String end = formatDate(selectedRange.get(selectedRange.size() - 1));

            dbRef.child(uid).child("cycle").child("startDate").setValue(start);
            dbRef.child(uid).child("cycle").child("endDate").setValue(end)
                    .addOnSuccessListener(aVoid -> {
                        textSelectedRange.setText("Haid: " + start + " - " + end + " ✅ Disimpan");
                        Toast.makeText(requireContext(), "Data siklus tersimpan", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private void restoreSavedCycle() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        dbRef.child(uid).child("cycle").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String startStr = snapshot.child("startDate").getValue(String.class);
                String endStr = snapshot.child("endDate").getValue(String.class);
                if (startStr == null || endStr == null) return;

                try {
                    Date startDt = SDF.parse(startStr);
                    Date endDt = SDF.parse(endStr);
                    if (startDt == null || endDt == null) return;

                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDt);
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endDt);

                    selectedRange.clear();
                    Calendar iter = (Calendar) startCal.clone();
                    while (!iter.after(endCal)) {
                        selectedRange.add(CalendarDay.from(iter));
                        iter.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    calendarView.removeDecorators();
                    calendarView.addDecorator(new RangeCircleDecorator(selectedRange, requireContext()));
                    textSelectedRange.setText("Haid tersimpan: " + startStr + " - " + endStr);

                } catch (ParseException e) {
                    e.printStackTrace();
                    textSelectedRange.setText("Data siklus tidak valid");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String formatDate(CalendarDay day) {
        return SDF.format(day.getDate().getTime());
    }

    public static class RangeCircleDecorator implements DayViewDecorator {
        private final List<CalendarDay> dates;
        private final Drawable highlightDrawable;

        public RangeCircleDecorator(List<CalendarDay> dates, android.content.Context context) {
            this.dates = new ArrayList<>(dates);
            this.highlightDrawable = ContextCompat.getDrawable(context, R.drawable.circle_red);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            if (highlightDrawable != null) {
                view.setBackgroundDrawable(highlightDrawable);
            }
        }
    }
}
