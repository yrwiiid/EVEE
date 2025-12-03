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
 * Halaman Calendar — versi lengkap terintegrasi API period_cycles:
 * - Load siklus (GET) dari ApiConfig.PERIOD_CYCLES_URL
 * - Simpan siklus (POST) ke ApiConfig.PERIOD_CYCLES_URL
 * - Hapus siklus hanya lokal (tidak ke server)
 *
 * Prasyarat:
 * - ApiConfig.PERIOD_CYCLES_URL terdefinisi
 * - SessionManager.getUserId() mengembalikan user_id
 * - AndroidManifest: <uses-permission android:name="android.permission.INTERNET" />
 */
public class halamancalendar extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView textSelectedRange;
    private Button btnSave, btnDelete;

    public static List<RangeData> savedRanges = new ArrayList<>();
    private final List<CalendarDay> selectedRange = new ArrayList<>();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

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

        // Interaksi kalender persis seperti versi lokal
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;

            // Jika klik pada range tersimpan → pilih seluruh range
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
                        SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            String start = obj.optString("start_date", "");
                            String end   = obj.optString("end_date", "");
                            if (start.isEmpty() || end.isEmpty()) continue;

                            Date dStart, dEnd;
                            try {
                                dStart = sdfApi.parse(start);
                                dEnd   = sdfApi.parse(end);
                            } catch (ParseException pe) {
                                // Skip record yang tidak bisa di-parse
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
    //  SIMPAN DATA (POST ke API)
    // ======================================
    private void saveCycle() {
        if (selectedRange.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih rentang tanggal dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String startUi = formatDate(selectedRange.get(0));                 // dd-MM-yyyy
        String endUi   = formatDate(selectedRange.get(selectedRange.size() - 1));

        String start = toApiDate(startUi);                                 // yyyy-MM-dd
        String end   = toApiDate(endUi);

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", sessionManager.getUserId());
            body.put("start_date", start);
            body.put("end_date", end);
            body.put("cycle_length", 28);                    // default contoh
            body.put("period_length", selectedRange.size()); // panjang range terpilih
            body.put("note", "");
        } catch (Exception e) { /* ignore */ }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.CALENDAR_URL,
                body,
                response -> {
                    Toast.makeText(getContext(), "Siklus berhasil disimpan ✓", Toast.LENGTH_SHORT).show();
                    // Refresh dari server agar ID dan konsistensi terjaga
                    loadCyclesFromServer();
                    // Reset pilihan aktif
                    selectedRange.clear();
                    textSelectedRange.setText("Belum ada tanggal dipilih");
                    refreshSelectedRangeUI();
                },
                error -> Toast.makeText(getContext(), "Gagal simpan siklus", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(req);
    }

    // ======================================
    //   HAPUS DATA (lokal saja, tidak ke server)
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
            // Hapus lokal (dekorasi kalender dibersihkan)
            savedRanges.remove(toRemove);
            Toast.makeText(requireContext(), "Rentang berhasil dihapus (lokal) ✓", Toast.LENGTH_SHORT).show();
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

        // Dekorasi data tersimpan
        for (RangeData rd : savedRanges) {
            calendarView.addDecorator(new RangeCircleDecorator(rd.range, requireContext()));
        }

        // Dekorasi pilihan aktif
        if (!selectedRange.isEmpty()) {
            calendarView.addDecorator(new RangeCircleDecorator(selectedRange, requireContext()));
        }
    }

    private String formatDate(CalendarDay day) {
        return SDF.format(day.getCalendar().getTime());
    }

    private String toApiDate(String ddMMyyyy) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = in.parse(ddMMyyyy);
            return d != null ? out.format(d) : ddMMyyyy;
        } catch (Exception e) {
            return ddMMyyyy;
        }
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
