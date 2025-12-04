package com.example.evee;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

public class SavedArtikelActivity extends AppCompatActivity {

    RecyclerView rvSaved;
    ImageView btnBack;
    SavedArtikelAdapter adapter;
    ArrayList<ArtikelModel> savedList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_saved_artikel);

        rvSaved = findViewById(R.id.rvSaved);
        btnBack = findViewById(R.id.btnBack);

        // Tombol back
        btnBack.setOnClickListener(v -> finish());

        rvSaved.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedArtikelAdapter(savedList, this);
        rvSaved.setAdapter(adapter);

        // Ambil user_id dari SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        if (userId != null) {
            loadSavedArticles(userId);
        } else {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedArticles(String userId) {
        String url = ApiConfig.BASE_URL + "user_articles.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    savedList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            // Tambahkan artikel ke savedList
                            savedList.add(new ArtikelModel(
                                    obj.getString("id"),          // ambil id artikel dari API
                                    obj.getString("title"),
                                    obj.optString("read_at", obj.optString("created_at")), // fallback
                                    obj.getString("image"),
                                    obj.optString("link", "")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error koneksi", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}
