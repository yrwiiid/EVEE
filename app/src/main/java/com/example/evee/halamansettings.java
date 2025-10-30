package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import androidx.appcompat.widget.AppCompatImageView; // Tambahan

public class halamansettings extends Fragment {

    private LinearLayout menuChangePassword, menuHelp, menuAbout, layoutHelpDetail, layoutAboutDetail;
    private AppCompatImageView iconArrowHelp, iconArrowAbout; // Tambahan (untuk panah)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.halaman_settings, container, false);

        // Inisialisasi komponen
        menuChangePassword = view.findViewById(R.id.menuChangePassword);
        menuHelp = view.findViewById(R.id.menuHelp);
        menuAbout = view.findViewById(R.id.menuAbout);
        layoutHelpDetail = view.findViewById(R.id.layoutHelpDetail);
        iconArrowHelp = view.findViewById(R.id.iconArrowHelp);
        layoutAboutDetail = view.findViewById(R.id.layoutAboutDetail); // tambahan
        iconArrowAbout = view.findViewById(R.id.iconArrowAbout); // tambahan

        // Klik "Ganti Password"
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Klik "Bantuan" -> Tampilkan/Sembunyikan detail
        menuHelp.setOnClickListener(v -> {
            if (layoutHelpDetail.getVisibility() == View.GONE) {
                layoutHelpDetail.setVisibility(View.VISIBLE);
                if (iconArrowHelp != null)
                    iconArrowHelp.setImageResource(R.drawable.ic_arrow_up);
            } else {
                layoutHelpDetail.setVisibility(View.GONE);
                if (iconArrowHelp != null)
                    iconArrowHelp.setImageResource(R.drawable.ic_arrow_down);
            }
        });

        // Klik "Tentang Aplikasi" -> Tampilkan/Sembunyikan narasi panjang EVEE
        menuAbout.setOnClickListener(v -> {
            if (layoutAboutDetail != null) {
                if (layoutAboutDetail.getVisibility() == View.GONE) {
                    layoutAboutDetail.setVisibility(View.VISIBLE);
                    if (iconArrowAbout != null)
                        iconArrowAbout.setImageResource(R.drawable.ic_arrow_up);
                } else {
                    layoutAboutDetail.setVisibility(View.GONE);
                    if (iconArrowAbout != null)
                        iconArrowAbout.setImageResource(R.drawable.ic_arrow_down);
                }
            } else {
                Toast.makeText(getActivity(), "Buka halaman Tentang Aplikasi", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
