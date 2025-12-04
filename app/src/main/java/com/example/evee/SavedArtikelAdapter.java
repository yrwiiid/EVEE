package com.example.evee;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

        // load gambar dari URL
        Glide.with(holder.itemView.getContext())
                .load(artikel.getGambarUrl()) // langsung pakai full URL dari API
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.imgThumb);



        holder.itemView.setOnClickListener(v -> {
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
