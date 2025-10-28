package com.example.evee;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Tampilkan halaman home default saat pertama buka
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new halamanhome())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            // Gunakan if-else alih-alih switch-case
            if (id == R.id.nav_home) {
                selectedFragment = new halamanhome(); // nanti buat class halamannotes
            } else if (id == R.id.nav_calendar) {
                selectedFragment = new halamancalendar(); // nanti buat class halamancalendar
            } else if (id == R.id.nav_notes) {
                selectedFragment = new halamannotes();
            } else if (id == R.id.nav_mood) {
                selectedFragment = new halamanmood(); // nanti buat class halamanmood
            } else if (id == R.id.nav_profile) {
                selectedFragment = new halamanprofil(); // nanti buat class halamanprofil
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}
