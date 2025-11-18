package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class loginactivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView registerLink, forgotPasswordLink;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi view
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.buttonLogin);
        registerLink = findViewById(R.id.textViewRegister);
        forgotPasswordLink = findViewById(R.id.textViewForgotPassword);

        // Login button
        loginBtn.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            // Validasi input
            if (emailText.isEmpty()) {
                email.setError("Email harus diisi");
                email.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                email.setError("Format email tidak valid");
                email.requestFocus();
                return;
            }

            if (passwordText.isEmpty()) {
                password.setError("Password harus diisi");
                password.requestFocus();
                return;
            }

            // Login Admin Manual
            if (emailText.equals("admin@silomba.com") && passwordText.equals("123456")) {
                Toast.makeText(loginactivity.this, "Login sebagai Admin", Toast.LENGTH_SHORT).show();

                // 🔥 Kirim sinyal agar popup muncul di dashboard/main
                Intent i = new Intent(loginactivity.this, dashboard.class);
                i.putExtra("showMoodPopup", true);
                startActivity(i);
                finish();

                return;
            }

            // Login Firebase
            mAuth.signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(loginactivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();

                            // 🔥 Kirim sinyal popup
                            Intent i = new Intent(loginactivity.this, dashboard.class);
                            i.putExtra("showMoodPopup", true);
                            startActivity(i);
                            finish();

                        } else {
                            Toast.makeText(loginactivity.this,
                                    "Login gagal: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Link ke halaman register
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(loginactivity.this, registeractivity.class));
        });

        // Link ke halaman lupa password
        forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(loginactivity.this, ForgotPasswordActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Jika user sudah login → langsung ke dashboard
        if (mAuth.getCurrentUser() != null) {

            Intent intent = new Intent(loginactivity.this, dashboard.class);
            intent.putExtra("showMoodPopup", true);  // 🔥 tetap kirim popup saat auto login
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Kosongkan password setiap kembali ke login
        password.setText("");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
