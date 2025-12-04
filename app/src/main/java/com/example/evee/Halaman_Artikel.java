package com.example.evee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Halaman_Artikel extends Fragment {

    private RecyclerView rvArtikel;
    private SavedArtikelAdapter adapter;
    private ArrayList<ArtikelModel> artikelList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.halaman_artikel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvArtikel = view.findViewById(R.id.rvArtikel);
        rvArtikel.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SavedArtikelAdapter(artikelList, getContext());
        rvArtikel.setAdapter(adapter);

        // Search
        EditText cari = view.findViewById(R.id.cari);
        cari.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // Load artikel dari API
        loadArtikel();
    }

    private void loadArtikel() {
        String url = ApiConfig.ARTICLES_URL;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    artikelList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            artikelList.add(new ArtikelModel(
                                    obj.getString("title"),
                                    obj.getString("created_at"),
                                    obj.getString("image"),
                                    obj.getString("link")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error koneksi: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
