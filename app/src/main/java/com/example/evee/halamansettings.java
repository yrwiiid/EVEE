package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.widget.AppCompatImageView;
import android.graphics.Paint;

public class halamansettings extends Fragment {

    private LinearLayout menuChangePassword, menuHelp, menuAbout, layoutHelpDetail, layoutAboutDetail;
    private AppCompatImageView iconArrowHelp, iconArrowAbout, iconBack;
    private TextView emailText, emailAddressText, whatsappText;

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
        layoutAboutDetail = view.findViewById(R.id.layoutAboutDetail);
        iconArrowAbout = view.findViewById(R.id.iconArrowAbout);
        iconBack = view.findViewById(R.id.iconBack);

        // Email & WhatsApp
        emailText = view.findViewById(R.id.emailText);
        emailAddressText = view.findViewById(R.id.emailAddressText);
        whatsappText = view.findViewById(R.id.whatsappText);

        // Email address underline (biru)
        if (emailAddressText != null) {
            emailAddressText.setPaintFlags(emailAddressText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        // Tombol Back
        iconBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Ganti Password
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Bantuan (tampilkan/sembunyikan detail)
        menuHelp.setOnClickListener(v -> {
            if (layoutHelpDetail.getVisibility() == View.GONE) {
                layoutHelpDetail.setVisibility(View.VISIBLE);
                iconArrowHelp.setImageResource(R.drawable.ic_arrow_up);
            } else {
                layoutHelpDetail.setVisibility(View.GONE);
                iconArrowHelp.setImageResource(R.drawable.ic_arrow_down);
            }
        });

        // Tentang Aplikasi (tampilkan/sembunyikan detail)
        menuAbout.setOnClickListener(v -> {
            if (layoutAboutDetail.getVisibility() == View.GONE) {
                layoutAboutDetail.setVisibility(View.VISIBLE);
                iconArrowAbout.setImageResource(R.drawable.ic_arrow_up);
            } else {
                layoutAboutDetail.setVisibility(View.GONE);
                iconArrowAbout.setImageResource(R.drawable.ic_arrow_down);
            }
        });

        // Klik email address
        if (emailAddressText != null) {
            emailAddressText.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:nikmazumroh458@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan EVEE");
                intent.putExtra(Intent.EXTRA_TEXT, "Halo tim EVEE, saya ingin menanyakan...");
                try {
                    startActivity(Intent.createChooser(intent, "Kirim email menggunakan..."));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Tidak ada aplikasi email yang tersedia", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Klik WhatsApp
        if (whatsappText != null) {
            whatsappText.setOnClickListener(v -> {
                String phoneNumber = "+6281217600757";
                String message = Uri.encode("Halo, saya butuh bantuan mengenai aplikasi EVEE.");
                String url = "https://wa.me/" + phoneNumber + "?text=" + message;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "WhatsApp tidak terpasang di perangkat ini", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }
}
