package com.example.evee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SavedArtikelAdapter extends RecyclerView.Adapter<SavedArtikelAdapter.ViewHolder> {

    private ArrayList<ArtikelModel> list;
    private Context context;

    public SavedArtikelAdapter(ArrayList<ArtikelModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_saved_artikel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ArtikelModel artikel = list.get(position);

        holder.txtSavedTitle.setText(artikel.getJudul());
        holder.txtSavedDate.setText(artikel.getHariUpload());
        holder.imgSavedThumb.setImageResource(artikel.getGambar());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgSavedThumb;
        TextView txtSavedTitle, txtSavedDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSavedThumb = itemView.findViewById(R.id.imgSavedThumb);
            txtSavedTitle = itemView.findViewById(R.id.txtSavedTitle);
            txtSavedDate = itemView.findViewById(R.id.txtSavedDate);
        }
    }
}

