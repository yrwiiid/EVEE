package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class loginactivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView registerLink, forgotPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi view
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.buttonLogin);
        registerLink = findViewById(R.id.textViewRegister);
        forgotPasswordLink = findViewById(R.id.textViewForgotPassword);

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

            // Login Admin Manual (opsional)
            if (emailText.equals("admin@silomba.com") && passwordText.equals("123456")) {
                Toast.makeText(loginactivity.this, "Login sebagai Admin", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(loginactivity.this, dashboard.class);
                i.putExtra("showMoodPopup", true);
                startActivity(i);
                finish();
                return;
            }

            // Karena Firebase sudah dihapus → sementara anggap login sukses
            Toast.makeText(loginactivity.this,
                    "Login berhasil (belum tersambung ke server)",
                    Toast.LENGTH_SHORT).show();

            Intent i = new Intent(loginactivity.this, dashboard.class);
            i.putExtra("showMoodPopup", true);
            startActivity(i);
            finish();
        });

        registerLink.setOnClickListener(v ->
                startActivity(new Intent(loginactivity.this, registeractivity.class))
        );

        forgotPasswordLink.setOnClickListener(v ->
                startActivity(new Intent(loginactivity.this, ForgotPasswordActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        password.setText("");
    }
}
