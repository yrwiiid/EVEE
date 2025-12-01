package com.example.evee;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
    private Button btnSave, btnDelete;

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
                    .replace(R.id.nav_host_fragment, new AddNote())
                    .addToBackStack(null)
                    .commit();
        });

        // Tidak ada restore dari Firebase — sekarang lokal
        refreshSelectedRangeUI();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {

            if (!selected) return;

            // Jika klik pada range tersimpan → hanya select
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

            // Klik pertama → auto 5 hari
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

            // Klik hari selanjutnya untuk extend range
            CalendarDay lastDay = selectedRange.get(selectedRange.size() - 1);

            Calendar nextCal = (Calendar) lastDay.getCalendar().clone();
            nextCal.add(Calendar.DAY_OF_MONTH, 1);
            CalendarDay nextValid = CalendarDay.from(nextCal);

            if (date.equals(nextValid)) {

                selectedRange.add(date);

                textSelectedRange.setText("Haid: " +
                        formatDate(selectedRange.get(0)) + " - " +
                        formatDate(selectedRange.get(selectedRange.size() - 1)));

                refreshSelectedRangeUI();

            } else {
                Toast.makeText(requireContext(),
                        "Klik tanggal setelah hari terakhir untuk melanjutkan.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> saveCycle());
        btnDelete.setOnClickListener(v -> deleteSelectedRange());

        return view;
    }

    // ======================================
    //  SIMPAN DATA (VERSI LOKAL)
    // ======================================
    private void saveCycle() {
        if (selectedRange.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih rentang tanggal dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String start = formatDate(selectedRange.get(0));
        String end = formatDate(selectedRange.get(selectedRange.size() - 1));

        // Simpan lokal saja
        savedRanges.add(new RangeData(UUID.randomUUID().toString(), new ArrayList<>(selectedRange)));

        Toast.makeText(requireContext(), "Siklus berhasil disimpan ✓", Toast.LENGTH_SHORT).show();

        selectedRange.clear();
        textSelectedRange.setText("Belum ada tanggal dipilih");

        refreshSelectedRangeUI();
    }

    // ======================================
    //   HAPUS DATA
    // ======================================
    private void deleteSelectedRange() {

        if (selectedRange.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih rentang yang akan dihapus!", Toast.LENGTH_SHORT).show();
            return;
        }

        String start = formatDate(selectedRange.get(0));
        String end = formatDate(selectedRange.get(selectedRange.size() - 1));

        RangeData toRemove = null;

        for (RangeData rd : savedRanges) {
            if (formatDate(rd.range.get(0)).equals(start) &&
                    formatDate(rd.range.get(rd.range.size() - 1)).equals(end)) {

                toRemove = rd;
                break;
            }
        }

        if (toRemove != null) {
            savedRanges.remove(toRemove);
            Toast.makeText(requireContext(), "Rentang berhasil dihapus ✓", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Data tidak ditemukan.", Toast.LENGTH_SHORT).show();
        }

        selectedRange.clear();
        textSelectedRange.setText("Belum ada tanggal dipilih");

        refreshSelectedRangeUI();
    }

    // ======================================
    //  REFRESH UI
    // ======================================
    private void refreshSelectedRangeUI() {

        calendarView.removeDecorators();

        // dekorasi data tersimpan
        for (RangeData rd : savedRanges) {
            calendarView.addDecorator(new RangeCircleDecorator(rd.range, requireContext()));
        }

        // dekorasi pilihan aktif
        if (!selectedRange.isEmpty()) {
            calendarView.addDecorator(new RangeCircleDecorator(selectedRange, requireContext()));
        }
    }

    private String formatDate(CalendarDay day) {
        return SDF.format(day.getCalendar().getTime());
    }

    // ======================================
    //  MODEL RANGE
    // ======================================
    private static class RangeData {
        String id;
        List<CalendarDay> range;
        RangeData(String id, List<CalendarDay> range) {
            this.id = id;
            this.range = range;
        }
    }

    // ======================================
    //  DECORATOR HAID
    // ======================================
    public static class RangeCircleDecorator implements DayViewDecorator {
        private final List<CalendarDay> dates;
        private final Drawable drawable;

        public RangeCircleDecorator(List<CalendarDay> dates, android.content.Context ctx) {
            this.dates = new ArrayList<>(dates);
            this.drawable = ContextCompat.getDrawable(ctx, R.drawable.circle_red);
        }

        @Override public boolean shouldDecorate(CalendarDay day) { return dates.contains(day); }
        @Override public void decorate(DayViewFacade view) { if (drawable != null) view.setBackgroundDrawable(drawable); }
    }
}
