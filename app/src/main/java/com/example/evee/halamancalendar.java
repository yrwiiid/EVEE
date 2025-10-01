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

        // restore semua riwayat siklus
        restoreSavedCycle();

        // klik tanggal
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;

            if (startDate == null) {
                // klik pertama
                startDate = date;
                endDate = null;
                selectedRange.clear();
                textSelectedRange.setText("Mulai: " + formatDate(startDate));
            } else {
                // klik kedua
                endDate = date;

                // jika sama → 1 hari
                if (startDate.equals(endDate)) {
                    selectedRange.clear();
                    selectedRange.add(startDate);

                    textSelectedRange.setText("Haid: " + formatDate(startDate));
                    startDate = null;
                    endDate = null;
                    return;
                }

                // pastikan urut
                Calendar startCal = (Calendar) startDate.getCalendar().clone();
                Calendar endCal = (Calendar) endDate.getCalendar().clone();
                if (startCal.after(endCal)) {
                    CalendarDay tmp = startDate;
                    startDate = endDate;
                    endDate = tmp;

                    startCal = (Calendar) startDate.getCalendar().clone();
                    endCal = (Calendar) endDate.getCalendar().clone();
                }

                // buat range
                selectedRange.clear();
                Calendar iter = (Calendar) startCal.clone();
                while (!iter.after(endCal)) {
                    selectedRange.add(CalendarDay.from(iter));
                    iter.add(Calendar.DAY_OF_MONTH, 1);
                }

                textSelectedRange.setText("Haid: " + formatDate(startDate) + " - " + formatDate(endDate));
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

            // simpan ke riwayat (push)
            String cycleId = dbRef.child(uid).child("cycles").push().getKey();
            if (cycleId != null) {
                dbRef.child(uid).child("cycles").child(cycleId).child("startDate").setValue(start);
                dbRef.child(uid).child("cycles").child(cycleId).child("endDate").setValue(end)
                        .addOnSuccessListener(aVoid -> {
                            textSelectedRange.setText("Haid: " + start + " - " + end + " ✅ Disimpan");
                            Toast.makeText(requireContext(), "Data siklus tersimpan", Toast.LENGTH_SHORT).show();
                            restoreSavedCycle(); // refresh kalender
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        return view;
    }

    private void restoreSavedCycle() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        dbRef.child(uid).child("cycles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                calendarView.removeDecorators(); // hapus dekorasi lama
                List<CalendarDay> lastCycleRange = new ArrayList<>();
                Date lastStartDt = null;
                Date lastEndDt = null;

                for (DataSnapshot cycleSnap : snapshot.getChildren()) {
                    String startStr = cycleSnap.child("startDate").getValue(String.class);
                    String endStr = cycleSnap.child("endDate").getValue(String.class);
                    if (startStr == null || endStr == null) continue;

                    try {
                        Date startDt = SDF.parse(startStr);
                        Date endDt = SDF.parse(endStr);
                        if (startDt == null || endDt == null) continue;

                        Calendar startCal = Calendar.getInstance();
                        startCal.setTime(startDt);
                        Calendar endCal = Calendar.getInstance();
                        endCal.setTime(endDt);

                        List<CalendarDay> range = new ArrayList<>();
                        Calendar iter = (Calendar) startCal.clone();
                        while (!iter.after(endCal)) {
                            range.add(CalendarDay.from(iter));
                            iter.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        // dekorasi siklus tersimpan
                        calendarView.addDecorator(new RangeCircleDecorator(range, requireContext()));

                        // simpan siklus terakhir untuk prediksi
                        lastCycleRange = range;
                        lastStartDt = startDt;
                        lastEndDt = endDt;

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // buat prediksi dari siklus terakhir
                if (lastStartDt != null && lastEndDt != null) {
                    long diff = (lastEndDt.getTime() - lastStartDt.getTime()) / (1000 * 60 * 60 * 24);
                    int periodLength = (int) diff + 1; // durasi haid
                    int cycleLength = 28; // asumsi siklus normal

                    Calendar nextStart = Calendar.getInstance();
                    nextStart.setTime(lastStartDt);
                    nextStart.add(Calendar.DAY_OF_MONTH, cycleLength);

                    Calendar nextEnd = (Calendar) nextStart.clone();
                    nextEnd.add(Calendar.DAY_OF_MONTH, periodLength - 1);

                    List<CalendarDay> predictedRange = new ArrayList<>();
                    Calendar iter2 = (Calendar) nextStart.clone();
                    while (!iter2.after(nextEnd)) {
                        predictedRange.add(CalendarDay.from(iter2));
                        iter2.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    // tambahkan dekorasi prediksi
                    calendarView.addDecorator(new RangeCircleDecoratorPrediksi(predictedRange, requireContext()));
                }

                textSelectedRange.setText("Riwayat & prediksi siklus ditampilkan ✅");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String formatDate(CalendarDay day) {
        return SDF.format(day.getCalendar().getTime());
    }

    // Dekorasi siklus tersimpan (merah)
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

    // Dekorasi prediksi (pink)
    public static class RangeCircleDecoratorPrediksi implements DayViewDecorator {
        private final List<CalendarDay> dates;
        private final Drawable highlightDrawable;

        public RangeCircleDecoratorPrediksi(List<CalendarDay> dates, android.content.Context context) {
            this.dates = new ArrayList<>(dates);
            this.highlightDrawable = ContextCompat.getDrawable(context, R.drawable.circle_pink);
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
