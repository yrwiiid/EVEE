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
    import android.widget.ImageButton;


    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.SetOptions;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.prolificinteractive.materialcalendarview.CalendarDay;
    import com.prolificinteractive.materialcalendarview.DayViewDecorator;
    import com.prolificinteractive.materialcalendarview.DayViewFacade;
    import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.*;

    public class halamancalendar extends Fragment {

        private MaterialCalendarView calendarView;
        private TextView textSelectedRange;
        private Button btnSave;
        private Button btnDelete;

        private FirebaseAuth mAuth;
        private FirebaseFirestore db;

        private CalendarDay startDate = null;
        private CalendarDay endDate = null;

        // Untuk menyimpan tanggal beserta documentId agar bisa delete
        private final List<RangeData> savedRanges = new ArrayList<>();
        private final List<CalendarDay> selectedRange = new ArrayList<>();
        private final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.halamancalendar, container, false);

            calendarView = view.findViewById(R.id.calendarView);
            textSelectedRange = view.findViewById(R.id.textSelectedRange);
            btnSave = view.findViewById(R.id.btnSavePeriod);
            btnDelete = view.findViewById(R.id.btnDeletePeriod);

            ImageButton btnAddNote = view.findViewById(R.id.btnAddNote);
            btnAddNote.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new AddNote())   // INI YANG BENAR
                        .addToBackStack(null)
                        .commit();
            });



            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            restoreSavedCycle();

            calendarView.setOnDateChangedListener((widget, date, selected) -> {
                if (!selected) return;

                // Klik pada range yang sudah tersimpan → hanya pilih, TIDAK mengubah
                for (RangeData rangeData : savedRanges) {
                    if (rangeData.range.contains(date)) {
                        selectedRange.clear();
                        selectedRange.addAll(rangeData.range);
                        textSelectedRange.setText("Haid: " +
                                formatDate(selectedRange.get(0)) + " - " +
                                formatDate(selectedRange.get(selectedRange.size() - 1)) +
                                " (terpilih)");

                        refreshSelectedRangeUI();
                        return;
                    }
                }

                // Klik pertama: auto 5 hari
                if (selectedRange.isEmpty()) {
                    Calendar cal = (Calendar) date.getCalendar().clone();
                    selectedRange.clear();

                    for (int i = 0; i < 5; i++) {
                        selectedRange.add(CalendarDay.from((Calendar) cal.clone()));
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    textSelectedRange.setText("Haid: " +
                            formatDate(selectedRange.get(0)) + " - " +
                            formatDate(selectedRange.get(selectedRange.size() - 1)));

                    refreshSelectedRangeUI();
                    return;
                }

                // Klik setelah hari terakhir → tambah 1 hari
                CalendarDay lastDay = selectedRange.get(selectedRange.size() - 1);
                Calendar lastCal = (Calendar) lastDay.getCalendar().clone();
                lastCal.add(Calendar.DAY_OF_MONTH, 1);
                CalendarDay nextValidDay = CalendarDay.from(lastCal);

                if (date.equals(nextValidDay)) {
                    selectedRange.add(date);

                    textSelectedRange.setText("Haid: " +
                            formatDate(selectedRange.get(0)) + " - " +
                            formatDate(selectedRange.get(selectedRange.size() - 1)));

                    refreshSelectedRangeUI();

                } else {
                    Toast.makeText(requireContext(),
                            "Untuk melanjutkan haid, klik hari setelah tanggal terakhir.",
                            Toast.LENGTH_SHORT).show();
                }
            });



            btnSave.setOnClickListener(v -> saveCycle());
            btnDelete.setOnClickListener(v -> deleteSelectedRange());




            return view;
        }


        private void refreshSelectedRangeUI() {
            calendarView.removeDecorators();

            // Tambahkan ulang semua data tersimpan
            for (RangeData rd : savedRanges) {
                calendarView.addDecorator(new RangeCircleDecorator(rd.range, requireContext()));
            }

            // Tambahkan dekorasi seleksi yang sedang diedit
            if (!selectedRange.isEmpty()) {
                calendarView.addDecorator(new RangeCircleDecorator(selectedRange, requireContext()));
            }
        }


        private void saveCycle() {
            if (selectedRange.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih rentang tanggal dulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "User belum login.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            Map<String,Object> cycle = new HashMap<>();
            cycle.put("startDate", formatDate(selectedRange.get(0)));
            cycle.put("endDate", formatDate(selectedRange.get(selectedRange.size()-1)));
            cycle.put("durationDays", selectedRange.size());

            db.collection("Users").document(uid).collection("cycles")
                    .add(cycle)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(requireContext(), "Data siklus tersimpan ✅", Toast.LENGTH_SHORT).show();
                        selectedRange.clear();
                        textSelectedRange.setText("Belum ada tanggal dipilih");
                        restoreSavedCycle();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        private void deleteSelectedRange() {
            if (selectedRange.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih rentang dulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;
            String uid = user.getUid();

            // cari documentId dari savedRanges
            String start = formatDate(selectedRange.get(0));
            String end = formatDate(selectedRange.get(selectedRange.size()-1));
            String docIdToDelete = null;

            for (RangeData rd : savedRanges) {
                if (formatDate(rd.range.get(0)).equals(start) &&
                        formatDate(rd.range.get(rd.range.size()-1)).equals(end)) {
                    docIdToDelete = rd.documentId;
                    break;
                }
            }

            if (docIdToDelete == null) {
                Toast.makeText(requireContext(), "Data tidak ditemukan.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("Users").document(uid).collection("cycles").document(docIdToDelete)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Rentang berhasil dihapus ✅", Toast.LENGTH_SHORT).show();
                        selectedRange.clear();
                        textSelectedRange.setText("Belum ada tanggal dipilih");
                        restoreSavedCycle();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Gagal hapus: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        private void restoreSavedCycle() {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;
            String uid = user.getUid();

            db.collection("Users").document(uid).collection("cycles")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        calendarView.removeDecorators();
                        savedRanges.clear();

                        if (querySnapshot.isEmpty()) {
                            textSelectedRange.setText("Belum ada data siklus tersimpan");
                            return;
                        }

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

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(startDt);
                                Calendar endCal = Calendar.getInstance();
                                endCal.setTime(endDt);

                                List<CalendarDay> range = new ArrayList<>();
                                while (!cal.after(endCal)) {
                                    range.add(CalendarDay.from(cal));
                                    cal.add(Calendar.DAY_OF_MONTH,1);
                                }

                                savedRanges.add(new RangeData(doc.getId(), range));
                                calendarView.addDecorator(new RangeCircleDecorator(range, requireContext()));

                                startDates.add(startDt);
                                durations.add(durationDays);
                                lastStartDt = startDt;

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        // hitung rata-rata & prediksi
                        if (!startDates.isEmpty()) {
                            int averageDuration = 5;
                            int averageCycle = 28;

                            if (startDates.size() > 1) {
                                Collections.sort(startDates);
                                int totalDiff = 0;
                                for (int i = 1; i < startDates.size(); i++) {
                                    long diff = (startDates.get(i).getTime() - startDates.get(i-1).getTime()) / (1000*60*60*24);
                                    totalDiff += diff;
                                }
                                averageCycle = totalDiff / (startDates.size()-1);
                            }

                            int totalDur = 0;
                            for (int d : durations) totalDur += d;
                            averageDuration = totalDur / durations.size();

                            textSelectedRange.setText("Rata-rata siklus: " + averageCycle + " hari, rata-rata haid: " + averageDuration + " hari ✅");

                            if (lastStartDt != null) {
                                Calendar nextStart = Calendar.getInstance();
                                nextStart.setTime(lastStartDt);
                                nextStart.add(Calendar.DAY_OF_MONTH, averageCycle);

                                Calendar nextEnd = (Calendar) nextStart.clone();
                                nextEnd.add(Calendar.DAY_OF_MONTH, averageDuration-1);

                                List<CalendarDay> predictedRange = new ArrayList<>();
                                Calendar iter = (Calendar) nextStart.clone();
                                while (!iter.after(nextEnd)) {
                                    predictedRange.add(CalendarDay.from(iter));
                                    iter.add(Calendar.DAY_OF_MONTH,1);
                                }
                                calendarView.addDecorator(new RangeCircleDecoratorPrediksi(predictedRange, requireContext()));
                            }
                        }

                    });
        }

        private String formatDate(CalendarDay day) {
            return SDF.format(day.getCalendar().getTime());
        }

        // Model data untuk menyimpan documentId + range
        private static class RangeData {
            String documentId;
            List<CalendarDay> range;
            RangeData(String documentId, List<CalendarDay> range) {
                this.documentId = documentId;
                this.range = range;
            }
        }

        public static class RangeCircleDecorator implements DayViewDecorator {
            private final List<CalendarDay> dates;
            private final Drawable highlightDrawable;
            public RangeCircleDecorator(List<CalendarDay> dates, android.content.Context context) {
                this.dates = new ArrayList<>(dates);
                this.highlightDrawable = ContextCompat.getDrawable(context, R.drawable.circle_red);
            }
            @Override
            public boolean shouldDecorate(CalendarDay day) { return dates.contains(day); }
            @Override
            public void decorate(DayViewFacade view) { if(highlightDrawable!=null)view.setBackgroundDrawable(highlightDrawable);}
        }

        public static class RangeCircleDecoratorPrediksi implements DayViewDecorator {
            private final List<CalendarDay> dates;
            private final Drawable highlightDrawable;
            public RangeCircleDecoratorPrediksi(List<CalendarDay> dates, android.content.Context context) {
                this.dates = new ArrayList<>(dates);
                this.highlightDrawable = ContextCompat.getDrawable(context, R.drawable.circle_pink);
            }
            @Override
            public boolean shouldDecorate(CalendarDay day) { return dates.contains(day);}
            @Override
            public void decorate(DayViewFacade view) { if(highlightDrawable!=null)view.setBackgroundDrawable(highlightDrawable);}
        }
    }
