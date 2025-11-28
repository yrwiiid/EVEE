package com.example.evee;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MenstruasiActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menstruasi);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_fragment, new ScreeningFragment())
                    .commit();
        }
    }
}

