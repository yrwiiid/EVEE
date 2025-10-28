package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splashscreen extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 detik

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bisa tambahin layout animasi splash
        setContentView(R.layout.splashscreen);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // sudah login → langsung ke home
                startActivity(new Intent(splashscreen.this, dashboard.class));
            } else {
                // belum login → ke register / login
                startActivity(new Intent(splashscreen.this, loginactivity.class));
            }
            finish(); // biar tidak bisa back ke splash
        }, SPLASH_TIME_OUT);
    }
}
