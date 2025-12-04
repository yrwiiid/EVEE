package com.example.evee;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.annotations.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;

public class SavedArtikelAdapter extends RecyclerView.Adapter<SavedArtikelAdapter.ViewHolder> {

    private ArrayList<ArtikelModel> list;
    private ArrayList<ArtikelModel> listFull;
    private Context context;

    public SavedArtikelAdapter(ArrayList<ArtikelModel> list, Context context) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_artikel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArtikelModel artikel = list.get(position);

        holder.txtTitle.setText(artikel.getJudul());
        holder.txtDate.setText(artikel.getTanggal());

        Glide.with(holder.itemView.getContext())
                .load(artikel.getGambarUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Gagal load gambar", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d("GlideSuccess", "Gambar berhasil dimuat");
                        return false;
                    }
                })
                .into(holder.imgThumb);




        holder.itemView.setOnClickListener(v -> {
            // Catat ke database user_articles
            try {
                SessionManager sessionManager = new SessionManager(context);
                String userId = sessionManager.getUserId();

                JSONObject body = new JSONObject();
                body.put("user_id", userId);
                body.put("article_id", artikel.getId()); // pastikan ArtikelModel punya field id
                body.put("saved", 0);

                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiConfig.BASE_URL + "user_articles.php",
                        body,
                        response -> Log.d("API", "Berhasil catat baca: " + response),
                        error -> Log.e("API", "Gagal catat baca: " + error.getMessage())
                );

                Volley.newRequestQueue(context).add(req);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Setelah dicatat, buka artikel
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(artikel.getLink()));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // fungsi filter untuk search
    public void filter(String keyword) {
        list.clear();
        if (keyword.isEmpty()) {
            list.addAll(listFull);
        } else {
            for (ArtikelModel artikel : listFull) {
                if (artikel.getJudul().toLowerCase().contains(keyword.toLowerCase())) {
                    list.add(artikel);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView txtTitle, txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
