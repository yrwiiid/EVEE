package com.example.evee;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SavedArtikelActivity extends AppCompatActivity {

    RecyclerView rvSaved;
    ImageView btnBack;
    SavedArtikelAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_saved_artikel);

        rvSaved = findViewById(R.id.rvSaved);
        btnBack = findViewById(R.id.btnBack);

        // Tombol back
        btnBack.setOnClickListener(v -> finish());

        ArrayList<ArtikelModel> savedList =
                (ArrayList<ArtikelModel>) getIntent().getSerializableExtra("saved");

        if (savedList == null) savedList = new ArrayList<>();

        adapter = new SavedArtikelAdapter(savedList, this);
        rvSaved.setLayoutManager(new LinearLayoutManager(this));
        rvSaved.setAdapter(adapter);

    }
}
