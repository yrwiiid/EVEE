package com.example.evee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class Halaman_Artikel extends Fragment {

    // Artikel 1
    TextView judulartikel;
    ImageView imgArtikel1, simpan;

    // Artikel 2
    TextView judulartikel2;
    ImageView imgArtikel2, simpan2;

    // Artikel 3
    TextView judulartikel3;
    ImageView imgArtikel3, simpan3;

    // Simpan utama
    ImageView simpanuatama;

    // List artikel tersimpan
    // List artikel tersimpan
    private List<ArtikelModel> artikelTersimpan = new ArrayList<>();

    private void hapusArtikel(String judul) {
        for (int i = 0; i < artikelTersimpan.size(); i++) {
            if (artikelTersimpan.get(i).getJudul().equals(judul)) {
                artikelTersimpan.remove(i);
                break;
            }
        }
    }


    // Status simpan masing-masing artikel
    private boolean isSaved1 = false;
    private boolean isSaved2 = false;
    private boolean isSaved3 = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.halaman_artikel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ========================
        // === INISIALISASI VIEW ===
        // ========================
        imgArtikel1 = view.findViewById(R.id.imgArtikel1);
        judulartikel = view.findViewById(R.id.judulartikel);
        simpan = view.findViewById(R.id.simpan);

        imgArtikel2 = view.findViewById(R.id.imgArtikel2);
        judulartikel2 = view.findViewById(R.id.judulartikel2);
        simpan2 = view.findViewById(R.id.simpan2);

        imgArtikel3 = view.findViewById(R.id.imgArtikel3);
        judulartikel3 = view.findViewById(R.id.judulartikel3);
        simpan3 = view.findViewById(R.id.simpan3);

        simpanuatama = view.findViewById(R.id.simpanuatama);

        // Search
        EditText cari = view.findViewById(R.id.cari);

        // Card Views
        View card1 = (View) imgArtikel1.getParent();
        View card2 = (View) view.findViewById(R.id.imgArtikel2).getParent();
        View card3 = (View) view.findViewById(R.id.imgArtikel3).getParent();

        // Judul
        TextView judul1 = view.findViewById(R.id.judulartikel);
        TextView judul2 = view.findViewById(R.id.judulartikel2);
        TextView judul3 = view.findViewById(R.id.judulartikel3);

        // ========================
        // === SEARCH LISTENER ===
        // ========================
        cari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase();

                card1.setVisibility(judul1.getText().toString().toLowerCase().contains(keyword) ? View.VISIBLE : View.GONE);
                card2.setVisibility(judul2.getText().toString().toLowerCase().contains(keyword) ? View.VISIBLE : View.GONE);
                card3.setVisibility(judul3.getText().toString().toLowerCase().contains(keyword) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // ================================
        // === URL ARTIKEL ===
        // ================================
        String url1 = "https://rscarolus.or.id/artikel/mengenal-dan-mengatasi-nyeri-haid/";
        String url2 = "https://www.alodokter.com/8-obat-nyeri-haid-yang-ampuh-di-apotik";
        String url3 = "https://www.alodokter.com/yang-terjadi-selama-siklus-menstruasi";

        // ================================
        // === EVENT ARTIKEL 1 ===
        // ================================
        imgArtikel1.setOnClickListener(v -> bukaWeb(url1));
        judulartikel.setOnClickListener(v -> bukaWeb(url1));

        simpan.setOnClickListener(v -> {
            if (!isSaved1) {
                simpan.setImageResource(R.drawable.ic_simpan_filled);
                artikelTersimpan.add(new ArtikelModel(
                        judulartikel.getText().toString(),
                        "2 hari lalu",
                        R.drawable.artikel1
                ));
                Toast.makeText(getContext(), "Artikel 1 disimpan!", Toast.LENGTH_SHORT).show();
                isSaved1 = true;
            } else {
                simpan.setImageResource(R.drawable.ic_simpan_outline);
                hapusArtikel(judulartikel.getText().toString());
                Toast.makeText(getContext(), "Artikel 1 dihapus!", Toast.LENGTH_SHORT).show();
                isSaved1 = false;
            }
        });


        // ================================
        // === EVENT ARTIKEL 2 ===
        // ================================
        imgArtikel2.setOnClickListener(v -> bukaWeb(url2));
        judulartikel2.setOnClickListener(v -> bukaWeb(url2));

        simpan2.setOnClickListener(v -> {
            if (!isSaved2) {
                simpan2.setImageResource(R.drawable.ic_simpan_filled);
                artikelTersimpan.add(new ArtikelModel(
                        judulartikel2.getText().toString(),
                        "3 hari lalu",
                        R.drawable.artikel2
                ));
                Toast.makeText(getContext(), "Artikel 2 disimpan!", Toast.LENGTH_SHORT).show();
                isSaved2 = true;
            } else {
                simpan2.setImageResource(R.drawable.ic_simpan_outline);
                hapusArtikel(judulartikel2.getText().toString());
                Toast.makeText(getContext(), "Artikel 2 dihapus!", Toast.LENGTH_SHORT).show();
                isSaved2 = false;
            }
        });


        // ================================
        // === EVENT ARTIKEL 3 ===
        // ================================
        imgArtikel3.setOnClickListener(v -> bukaWeb(url3));
        judulartikel3.setOnClickListener(v -> bukaWeb(url3));

        simpan3.setOnClickListener(v -> {
            if (!isSaved3) {
                simpan3.setImageResource(R.drawable.ic_simpan_filled);
                artikelTersimpan.add(new ArtikelModel(
                        judulartikel3.getText().toString(),
                        "3 hari lalu",
                        R.drawable.artikel3
                ));
                Toast.makeText(getContext(), "Artikel 3 disimpan!", Toast.LENGTH_SHORT).show();
                isSaved3 = true;
            } else {
                simpan3.setImageResource(R.drawable.ic_simpan_outline);
                hapusArtikel(judulartikel3.getText().toString());
                Toast.makeText(getContext(), "Artikel 3 dihapus!", Toast.LENGTH_SHORT).show();
                isSaved3 = false;
            }
        });


        // ================================
        // === SIMPAN UTAMA ===
        // ================================
        // Ambil ID button dari layout
        simpanuatama = view.findViewById(R.id.simpanuatama);

        // Tambahkan listener di sini
        simpanuatama.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SavedArtikelActivity.class);
            intent.putExtra("saved", new ArrayList<>(artikelTersimpan)); // kirim model
            startActivity(intent);
        });
    }

    // ============================
    // === FUNGSI BUKA WEB ===
    // ============================
    private void bukaWeb(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }
}
