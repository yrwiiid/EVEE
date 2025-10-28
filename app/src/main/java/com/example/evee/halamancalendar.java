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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class halamancalendar extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView textSelectedRange;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();

        restoreSavedCycle();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;

            if (startDate == null) {
                startDate = date;
                endDate = null;
                selectedRange.clear();
                textSelectedRange.setText("Mulai: " + formatDate(startDate));
            } else {
                endDate = date;
                Calendar startCal = (Calendar) startDate.getCalendar().clone();
                Calendar endCal = (Calendar) endDate.getCalendar().clone();

                if (startCal.after(endCal)) {
                    CalendarDay tmp = startDate;
                    startDate = endDate;
                    endDate = tmp;
                    startCal = (Calendar) startDate.getCalendar().clone();
                    endCal = (Calendar) endDate.getCalendar().clone();
                }

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

        btnSave.setOnClickListener(v -> saveCycle());
        return view;
    }

    private void saveCycle() {
        if (selectedRange.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih rentang tanggal dulu (klik 2 tanggal).", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User belum login.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String start = formatDate(selectedRange.get(0));
        String end = formatDate(selectedRange.get(selectedRange.size() - 1));
        int duration = selectedRange.size(); // jumlah hari haid

        Map<String, Object> cycle = new HashMap<>();
        cycle.put("startDate", start);
        cycle.put("endDate", end);
        cycle.put("durationDays", duration);

        db.collection("users")
                .document(uid)
                .collection("cycles")
                .add(cycle)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(requireContext(), "Data siklus tersimpan ✅", Toast.LENGTH_SHORT).show();
                    restoreSavedCycle();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void restoreSavedCycle() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("cycles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        textSelectedRange.setText("Belum ada data siklus tersimpan.");
                        return;
                    }

                    calendarView.removeDecorators();

                    List<Date> startDates = new ArrayList<>();
                    List<Integer> durations = new ArrayList<>();
                    Date lastStartDt = null;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String startStr = doc.getString("startDate");
                        String endStr = doc.getString("endDate");
                        Long dur = doc.getLong("durationDays");

                        if (startStr == null || endStr == null) continue;
                        int durationDays = dur != null ? dur.intValue() : 5;

                        try {
                            Date startDt = SDF.parse(startStr);
                            Date endDt = SDF.parse(endStr);
                            if (startDt == null || endDt == null) continue;

                            // dekorasi periode haid aktual
                            List<CalendarDay> range = new ArrayList<>();
                            Calendar iter = Calendar.getInstance();
                            iter.setTime(startDt);
                            Calendar endCal = Calendar.getInstance();
                            endCal.setTime(endDt);

                            while (!iter.after(endCal)) {
                                range.add(CalendarDay.from(iter));
                                iter.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            calendarView.addDecorator(new RangeCircleDecorator(range, requireContext()));
                            startDates.add(startDt);
                            durations.add(durationDays);
                            lastStartDt = startDt;

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // hitung rata-rata siklus & durasi haid
                    if (startDates.size() > 1) {
                        Collections.sort(startDates);
                        int totalDiff = 0;
                        for (int i = 1; i < startDates.size(); i++) {
                            long diff = (startDates.get(i).getTime() - startDates.get(i - 1).getTime()) / (1000 * 60 * 60 * 24);
                            totalDiff += diff;
                        }
                        int averageCycle = totalDiff / (startDates.size() - 1);

                        int totalDur = 0;
                        for (int d : durations) totalDur += d;
                        int averageDuration = totalDur / durations.size();

                        Map<String, Object> update = new HashMap<>();
                        update.put("averageCycleLength", averageCycle);
                        update.put("averageDuration", averageDuration);
                        db.collection("users").document(uid).set(update, SetOptions.merge());

                        textSelectedRange.setText("Rata-rata siklus: " + averageCycle +
                                " hari, rata-rata haid: " + averageDuration + " hari ✅");

                        // prediksi berikutnya
                        if (lastStartDt != null) {
                            Calendar nextStart = Calendar.getInstance();
                            nextStart.setTime(lastStartDt);
                            nextStart.add(Calendar.DAY_OF_MONTH, averageCycle);

                            Calendar nextEnd = (Calendar) nextStart.clone();
                            nextEnd.add(Calendar.DAY_OF_MONTH, averageDuration - 1);

                            List<CalendarDay> predictedRange = new ArrayList<>();
                            Calendar iter2 = (Calendar) nextStart.clone();
                            while (!iter2.after(nextEnd)) {
                                predictedRange.add(CalendarDay.from(iter2));
                                iter2.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            calendarView.addDecorator(new RangeCircleDecoratorPrediksi(predictedRange, requireContext()));
                        }
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal ambil data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private String formatDate(CalendarDay day) {
        return SDF.format(day.getCalendar().getTime());
    }

    // Dekorasi warna merah = periode haid tersimpan
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
            if (highlightDrawable != null) view.setBackgroundDrawable(highlightDrawable);
        }
    }

    // Dekorasi warna pink = prediksi haid berikutnya
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
            if (highlightDrawable != null) view.setBackgroundDrawable(highlightDrawable);
        }
    }
}
