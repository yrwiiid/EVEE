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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Kalender haid:
 * - Input tanggal satu per satu (toggle).
 * - Simpan sebagai beberapa rentang berurutan (start_date–end_date).
 * - API tidak perlu diubah: dipanggil per rentang.
 */
public class halamancalendar extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView textSelectedRange;
    private Button btnSave, btnDelete;

    public static List<RangeData> savedRanges = new ArrayList<>(); // dari server
    private final List<CalendarDay> selectedDates = new ArrayList<>(); // pilihan user per tanggal
    private final SimpleDateFormat SDF_UI = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat SDF_API = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private SessionManager sessionManager;
    private RequestQueue requestQueue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halamancalendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        textSelectedRange = view.findViewById(R.id.textSelectedRange);
        btnSave = view.findViewById(R.id.btnSavePeriod);
        btnDelete = view.findViewById(R.id.btnDeletePeriod);

        sessionManager = new SessionManager(requireContext());
        requestQueue = Volley.newRequestQueue(requireContext());

        ImageButton btnAddNote = view.findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new AddNote())
                    .addToBackStack(null)
                    .commit();
        });

        // Load siklus dari server (mengisi savedRanges dan dekorasi)
        loadCyclesFromServer();

        // Input per tanggal: klik = toggle
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;

            // Toggle tanggal
            if (selectedDates.contains(date)) {
                selectedDates.remove(date);
            } else {
                selectedDates.add(date);
            }

            // Urutkan tanggal (naik)
            selectedDates.sort((d1, d2) -> d1.getDate().compareTo(d2.getDate()));

            // Update label
            updateSelectedLabel();

            // Refresh dekorasi
            refreshSelectedRangeUI();
        });

        btnSave.setOnClickListener(v -> saveCyclesGrouped());
        btnDelete.setOnClickListener(v -> clearSelection());

        return view;
    }

    // ======================================
    //  LOAD DATA (GET dari API)
    // ======================================
    private void loadCyclesFromServer() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.CALENDAR_URL + "?user_id=" + userId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    savedRanges.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            String start = obj.optString("start_date", "");
                            String end   = obj.optString("end_date", "");
                            if (start.isEmpty() || end.isEmpty()) continue;

                            Date dStart, dEnd;
                            try {
                                dStart = SDF_API.parse(start);
                                dEnd   = SDF_API.parse(end);
                            } catch (ParseException pe) {
                                continue;
                            }

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dStart);
                            Calendar endCal = Calendar.getInstance();
                            endCal.setTime(dEnd);

                            List<CalendarDay> range = new ArrayList<>();
                            while (!cal.after(endCal)) {
                                range.add(CalendarDay.from((Calendar) cal.clone()));
                                cal.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            savedRanges.add(new RangeData(
                                    obj.optString("id", UUID.randomUUID().toString()),
                                    range
                            ));
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Parse error data siklus", Toast.LENGTH_SHORT).show();
                    }
                    refreshSelectedRangeUI();
                },
                error -> Toast.makeText(getContext(), "Gagal load siklus", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(req);
    }

    // ======================================
    //  SIMPAN DATA (POST per rentang ke API)
    // ======================================
    private void saveCyclesGrouped() {
        if (selectedDates.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih tanggal haid dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group tanggal berurutan jadi rentang
        List<RangeData> grouped = groupContiguousDates(selectedDates);

        // Kirim tiap rentang ke server (API tetap: start_date & end_date)
        for (RangeData rd : grouped) {
            String startApi = toApiDate(rd.range.get(0));
            String endApi;

            if (rd.range.size() == 1) {
                // kalau hanya 1 hari, end = start
                endApi = startApi;
            } else {
                endApi = toApiDate(rd.range.get(rd.range.size() - 1));
            }


            JSONObject body = new JSONObject();
            try {
                body.put("user_id", sessionManager.getUserId());
                body.put("start_date", startApi);
                body.put("end_date", endApi);
                body.put("cycle_length", 28);                    // opsional default
                body.put("period_length", rd.range.size());      // panjang periode (hari)
                body.put("note", "");
            } catch (Exception ignore) {}

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.CALENDAR_URL,
                    body,
                    response -> {
                        // setelah salah satu rentang berhasil: refresh dekorasi dari server
                        loadCyclesFromServer();
                    },
                    error -> Toast.makeText(getContext(), "Gagal simpan rentang", Toast.LENGTH_SHORT).show()
            );

            requestQueue.add(req);
        }

        Toast.makeText(getContext(), "Siklus tersimpan ✓", Toast.LENGTH_SHORT).show();

        // Reset pilihan aktif
        selectedDates.clear();
        updateSelectedLabel();
        refreshSelectedRangeUI();
    }

    // Grouping tanggal berurutan (gap 1 hari) menjadi beberapa rentang
    private List<RangeData> groupContiguousDates(List<CalendarDay> dates) {
        List<RangeData> result = new ArrayList<>();
        if (dates.isEmpty()) return result;  // ← pakai () di sini

        // Pastikan urut
        List<CalendarDay> sorted = new ArrayList<>(dates);
        sorted.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        List<CalendarDay> current = new ArrayList<>();
        current.add(sorted.get(0));

        for (int i = 1; i < sorted.size(); i++) {
            CalendarDay prev = sorted.get(i - 1);
            CalendarDay cur  = sorted.get(i);

            if (isNextDay(prev, cur)) {
                current.add(cur);
            } else {
                result.add(new RangeData(UUID.randomUUID().toString(), new ArrayList<>(current)));
                current.clear();
                current.add(cur);
            }
        }
        result.add(new RangeData(UUID.randomUUID().toString(), new ArrayList<>(current)));
        return result;
    }


    private boolean isNextDay(CalendarDay prev, CalendarDay cur) {
        Calendar cal = (Calendar) prev.getCalendar().clone();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        CalendarDay next = CalendarDay.from(cal);
        return next.equals(cur);
    }

    // ======================================
    //   HAPUS PILIHAN (lokal saja)
    // ======================================
    private void clearSelection() {
        if (selectedDates.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada tanggal dipilih.", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDates.clear();
        updateSelectedLabel();
        refreshSelectedRangeUI();
        Toast.makeText(requireContext(), "Pilihan dibersihkan ✓", Toast.LENGTH_SHORT).show();
    }

    // ======================================
    //  REFRESH UI
    // ======================================
    private void refreshSelectedRangeUI() {
        calendarView.removeDecorators();

        // Dekorasi data tersimpan (dari server)
        for (RangeData rd : savedRanges) {
            calendarView.addDecorator(new RangeCircleDecorator(rd.range, requireContext()));
        }

        // Dekorasi pilihan aktif (tanggal-tanggal yang sedang dipilih)
        if (!selectedDates.isEmpty()) {
            calendarView.addDecorator(new RangeCircleDecorator(selectedDates, requireContext()));
        }
    }

    private void updateSelectedLabel() {
        if (selectedDates.isEmpty()) {
            textSelectedRange.setText("Belum ada tanggal dipilih");
        } else if (selectedDates.size() == 1) {
            textSelectedRange.setText("Haid: " + formatDate(selectedDates.get(0)));
        } else {
            // tampilkan dari tanggal pertama ke terakhir
            List<CalendarDay> sorted = new ArrayList<>(selectedDates);
            sorted.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            textSelectedRange.setText(
                    "Haid: " + formatDate(sorted.get(0)) + " - " + formatDate(sorted.get(sorted.size() - 1))
            );
        }
    }

    private String formatDate(CalendarDay day) {
        if (day == null || day.getCalendar() == null) return "";
        return SDF_UI.format(day.getCalendar().getTime());
    }

    private String toApiDate(CalendarDay day) {
        if (day == null || day.getCalendar() == null) return "";
        return SDF_API.format(day.getCalendar().getTime());
    }


    // ======================================
    //  MODEL RANGE
    // ======================================
    public static class RangeData {
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
