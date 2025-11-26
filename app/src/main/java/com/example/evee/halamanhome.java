    package com.example.evee;


    import android.os.Bundle;
    import android.os.Handler;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import androidx.cardview.widget.CardView;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;

    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.Locale;
    import java.util.List;
    import java.util.ArrayList;
    import java.util.concurrent.TimeUnit;
    import java.util.Date;


    public class halamanhome extends Fragment implements MoodUpdatePopup.OnMoodSavedListener {

        // UPDATE ID VIEW SESUAI XML BARU
        private TextView textGreeting, textDate, textCycle, textCycleStatus, textMoodDesc, textMoodComment;
        private ImageView imageMoodIcon;
        private CardView cardMood;
        private Button buttonEditMood; // Tombol edit mood
        private Button buttonLogPeriod; // Tombol "Catat periode" untuk buka kalender

        private FirebaseAuth mAuth;
        private FirebaseFirestore firestore;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        private final SimpleDateFormat displaySdf = new SimpleDateFormat("dd MMM", Locale.getDefault()); // Untuk tanggal tampilan

        // Pastikan urutan gambar sesuai urutan labels yang dipakai di popup
        private final int[] moodImages = {
                R.drawable.bingung, R.drawable.semangat, R.drawable.cemas, R.drawable.lelah,
                R.drawable.marah, R.drawable.senang, R.drawable.sedih, R.drawable.percayadiri,
                R.drawable.bosan, R.drawable.senang10, R.drawable.senang11, R.drawable.senang12
        };
        private final String[] labels = {
                "Bingung","Semangat","Cemas","Lelah","Marah","Senang",
                "Sedih","Percaya Diri","Bosan","Manja","Ngantuk","Sedih Banget"
        };
        private final String[] moodComments = {
                "Waduh, jangan bingung ya, tarik napas dulu~",
                "Ayo semangat! Kamu pasti bisa!",
                "Tenang, cemasnya hilang kalau kita senyum :)",
                "Lelah ya? Yuk istirahat sebentar~",
                "Ups, marah-marah nggak asik, yuk tarik napas!",
                "Yeay, senangnya ketemu hari yang ceria!",
                "Sedih ya? Peluk hangat buatmu 💛",
                "Percaya diri dong! Kamu hebat kok!",
                "Bosan? Yuk cari hal seru!",
                "Manja boleh, tapi jangan lupa senyum ya~",
                "Ngantuk? Tidur sebentar biar segar lagi!",
                "Sedih banget? Tenang, semuanya akan baik-baik saja!"
        };


        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.halaman_home, container, false);

            // ======= INISIALISASI VIEW (Update ID) =======
            textGreeting = view.findViewById(R.id.textGreeting);
            textDate = view.findViewById(R.id.textDate);
            textCycle = view.findViewById(R.id.textCycle);
            textCycleStatus = view.findViewById(R.id.textCycleStatus); // ID Baru
            textMoodDesc = view.findViewById(R.id.textMoodDesc);
            textMoodComment = view.findViewById(R.id.textMoodComment); // ID Baru
            imageMoodIcon = view.findViewById(R.id.imageMoodIcon);
            cardMood = view.findViewById(R.id.cardMood);
            buttonEditMood = view.findViewById(R.id.buttonEditMood); // ID Baru
            buttonLogPeriod = view.findViewById(R.id.buttonLogPeriod); // Tombol "Catat periode" di XML

            // ======= NAVIGASI: Tombol "Catat periode" -> buka halaman kalender =======
            if (buttonLogPeriod != null) {
                buttonLogPeriod.setOnClickListener(v -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, new halamancalendar())
                            .addToBackStack(null)
                            .commit();
                });
            }


            // Inisialisasi Firebase
            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();

            final FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String name = currentUser.getDisplayName();
                textGreeting.setText(name != null && !name.isEmpty()
                        ? "Halo, " + name + "!"
                        : "Halo, pengguna!");
            }

            String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());
            textDate.setText(todayDate);



            if (currentUser != null) {
                loadCycleData(currentUser.getUid());
                loadMoodData(currentUser.getUid());
                checkMoodTodayAndShowPopup(currentUser.getUid());
            }

            // ======= CLICK KARTU/TOMBOL MOOD (POPUP UPDATE) =======
            View.OnClickListener moodClickListener = v -> {
                if (currentUser == null) return;

                MoodUpdatePopup popup = new MoodUpdatePopup(getContext(), (moodLabel, moodEmoji) -> {
                    // Tampilkan deskripsi dan gambar mood
                    textMoodDesc.setText(moodLabel != null ? moodLabel : "Mood tersimpan");
                    textMoodComment.setText(getCommentForLabel(moodLabel)); // Set komentar mood

                    int resId = getDrawableForLabel(moodLabel);
                    if (resId != 0) {
                        imageMoodIcon.setImageResource(resId);
                        imageMoodIcon.setVisibility(View.VISIBLE);
                    }

                    String today = sdf.format(new Date());

                    // Simpan ke Firestore
                    firestore.collection("Users")
                            .document(currentUser.getUid())
                            .collection("Mood")
                            .document(today)
                            .set(new MoodData(moodLabel, moodEmoji))
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Mood berhasil diperbarui", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Gagal menyimpan mood", Toast.LENGTH_SHORT).show());
                });

                popup.show();
            };

            // Ganti CardView.setOnClickListener dengan Button.setOnClickListener
            if (buttonEditMood != null) buttonEditMood.setOnClickListener(moodClickListener);

            // ======= NAVIGASI: Tombol "Catat periode" -> buka halaman kalender =======
            if (buttonLogPeriod != null) {
                buttonLogPeriod.setOnClickListener(v -> {
                    // Ganti fragment ke halamancalendar
                    try {
                        // Pastikan menggunakan supportFragmentManager dari activity
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.nav_host_fragment, new halamancalendar())
                                .addToBackStack(null)
                                .commit();
                    } catch (IllegalStateException ise) {
                        // Jika fragment manager tidak tersedia, tampilkan toast
                        Toast.makeText(getContext(), "Tidak dapat membuka kalender saat ini.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return view;
        }

        // =====================================================================================
        // POPUP OTOMATIS — MENAMPILKAN MoodPopup (DENGAN TOMBOL SIMPAN)
        // =====================================================================================
        private void checkMoodTodayAndShowPopup(String uid) {
            String today = sdf.format(new Date());

            firestore.collection("Users")
                    .document(uid)
                    .collection("Mood")
                    .document(today)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            // Delay singkat agar UI sudah termuat sempurna
                            new Handler().postDelayed(() -> {
                                MoodPopup popup = new MoodPopup(getContext(), (moodLabel, moodEmoji) -> {
                                    // Update UI
                                    textMoodDesc.setText(moodLabel);
                                    textMoodComment.setText(getCommentForLabel(moodLabel)); // Set komentar mood

                                    // set image juga
                                    int resId = getDrawableForLabel(moodLabel);
                                    if (resId != 0 && imageMoodIcon != null) {
                                        imageMoodIcon.setImageResource(resId);
                                        imageMoodIcon.setVisibility(View.VISIBLE);
                                    }

                                    // Simpan ke Firestore
                                    firestore.collection("Users")
                                            .document(uid)
                                            .collection("Mood")
                                            .document(today)
                                            .set(new MoodData(moodLabel, moodEmoji));
                                });

                                popup.show();
                            }, 300);
                        }
                    });
        }

        // =====================================================================================
        // LOAD DATA SIKLUS (Diperbarui untuk tampilan baru)
        // =====================================================================================
        private void loadCycleData(String uid) {
            firestore.collection("Users")
                    .document(uid)
                    .collection("cycles")
                    .orderBy("startDate", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            textCycle.setText("Tidak ada data");
                            textCycleStatus.setText("Belum ada data siklus");
                            return;
                        }

                        List<Date> startDates = new ArrayList<>();
                        List<Integer> durations = new ArrayList<>();

                        for (DocumentSnapshot doc : query.getDocuments()) {
                            String startStr = doc.getString("startDate");
                            Long dur = doc.getLong("durationDays");
                            if (startStr == null) continue;

                            try {
                                Date start = sdf.parse(startStr);
                                if (start != null) {
                                    startDates.add(start);
                                    durations.add(dur != null ? dur.intValue() : 5); // default menstruasi 5 hari
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (startDates.isEmpty()) {
                            textCycle.setText("Tidak ada data");
                            textCycleStatus.setText("Data siklus tidak valid");
                            return;
                        }

                        // Hitung panjang siklus antar startDate
                        List<Integer> cycleLengths = new ArrayList<>();
                        for (int i = 1; i < startDates.size(); i++) {
                            long diff = (startDates.get(i).getTime() - startDates.get(i - 1).getTime()) / (1000 * 60 * 60 * 24);
                            cycleLengths.add((int) diff);
                        }

                        double avgCycle = cycleLengths.isEmpty() ? 28 : cycleLengths.stream().mapToInt(Integer::intValue).average().orElse(28);

                        int lastDuration = durations.get(durations.size() - 1);

                        Calendar today = Calendar.getInstance();
                        Calendar lastStart = Calendar.getInstance();
                        lastStart.setTime(startDates.get(startDates.size() - 1));

                        Calendar menstruationEnd = (Calendar) lastStart.clone();
                        menstruationEnd.add(Calendar.DAY_OF_MONTH, lastDuration - 1);

                        long daysSinceLastStart = TimeUnit.MILLISECONDS.toDays(today.getTimeInMillis() - lastStart.getTimeInMillis()) + 1;

                        if (!today.after(menstruationEnd)) {
                            // Sedang menstruasi
                            textCycle.setText(daysSinceLastStart + " Hari");
                            textCycleStatus.setText("Siklus saat ini: Menstruasi (Hari ke-" + daysSinceLastStart + ")");
                        } else {
                            // Hitung fase lain
                            int follicularDays = (int)Math.round(avgCycle) - lastDuration; // sisa hari sampai siklus berikutnya

                            int ovulationDay = lastDuration + 14; // hari ovulasi kira-kira 14 hari setelah menstruasi dimulai
                            int lutealDays = (int)Math.round(avgCycle) - ovulationDay;

                            long daysAfterMenstruation = daysSinceLastStart - lastDuration;

                            String phase = "";
                            if (daysAfterMenstruation < 0) {
                                phase = "Menstruasi";
                            } else if (daysAfterMenstruation < 14) {
                                phase = "Folikular";
                            } else if (daysAfterMenstruation == 14) {
                                phase = "Ovulasi";
                            } else if (daysAfterMenstruation < avgCycle) {
                                phase = "Luteal";
                            } else {
                                phase = "Menuju Menstruasi Berikutnya";
                            }

                            Calendar nextStart = (Calendar) lastStart.clone();
                            nextStart.add(Calendar.DAY_OF_MONTH, (int)Math.round(avgCycle));

                            long remainingDays = TimeUnit.MILLISECONDS.toDays(nextStart.getTimeInMillis() - today.getTimeInMillis());

                            textCycle.setText(remainingDays + " Hari Lagi");
                            textCycleStatus.setText("Siklus saat ini: " + phase + ", " + remainingDays + " hari lagi sampai menstruasi");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textCycle.setText("Error");
                        textCycleStatus.setText("Gagal memuat data");
                    });
        }





        // =====================================================================================
        // LOAD MOOD HARI INI (Diperbarui untuk tampilan baru)
        // =====================================================================================
        private void loadMoodData(String uid) {
            String today = sdf.format(new Date());

            firestore.collection("Users")
                    .document(uid)
                    .collection("Mood")
                    .document(today)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String mood = document.getString("mood");

                            textMoodDesc.setText(mood != null ? mood : "Mood Hari Ini");
                            textMoodComment.setText(getCommentForLabel(mood)); // Set komentar

                            int resId = getDrawableForLabel(mood);
                            if (resId != 0) {
                                imageMoodIcon.setImageResource(resId);
                                imageMoodIcon.setVisibility(View.VISIBLE);
                            } else {
                                imageMoodIcon.setVisibility(View.GONE);
                            }
                        } else {
                            textMoodDesc.setText("Belum ada mood");
                            textMoodComment.setText("Yuk, catat mood-mu hari ini!");
                            imageMoodIcon.setVisibility(View.GONE);
                        }
                    });
        }

        // =====================================================================================
        // GENERATE KALENDER (Telah Diperbaiki untuk Posisi Tengah)
        // =====================================================================================





        // =====================================================================================
        // CALLBACK DARI MoodUpdatePopup (Diperbarui untuk tampilan baru)
        // =====================================================================================
        @Override
        public void onMoodSaved(String moodLabel, String moodEmoji) {
            if (getActivity() == null) return;

            textMoodDesc.setText(moodLabel != null ? moodLabel : "Mood tersimpan");
            textMoodComment.setText(getCommentForLabel(moodLabel)); // Set komentar

            int resId = getDrawableForLabel(moodLabel);
            if (resId != 0) {
                imageMoodIcon.setImageResource(resId);
                imageMoodIcon.setVisibility(View.VISIBLE);
            } else {
                imageMoodIcon.setVisibility(View.GONE);
            }
        }

        // =====================================================================================
        // ===== Helper: map label -> drawable resource id (returns 0 jika tidak ada)
        // =====================================================================================
        private int getDrawableForLabel(String label) {
            if (label == null) return 0;

            for (int i = 0; i < labels.length; i++) {
                if (label.equals(labels[i])) {
                    if (i >= 0 && i < moodImages.length) return moodImages[i];
                }
            }
            return 0;
        }

        // ===== Helper: map label -> comment
        private String getCommentForLabel(String label) {
            if (label == null) return "Yuk, catat mood-mu hari ini!";

            for (int i = 0; i < labels.length; i++) {
                if (label.equals(labels[i])) {
                    if (i >= 0 && i < moodComments.length) return moodComments[i];
                }
            }
            return "Lanjutkan hal positif ya!";
        }

        // =====================================================================================
        // CLASS DATA (Tidak Berubah)
        // =====================================================================================
        public static class MoodData {
            public String mood;
            public String emoji;

            public MoodData() {}
            public MoodData(String mood, String emoji) {
                this.mood = mood;
                this.emoji = emoji;
            }
        }
    }
