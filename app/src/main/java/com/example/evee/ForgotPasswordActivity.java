package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText editTextForgot;
    private Button buttonResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lupa_password);

        editTextForgot = findViewById(R.id.editTextForgot);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        buttonResetPassword.setOnClickListener(v -> {
            String email = editTextForgot.getText() != null
                    ? editTextForgot.getText().toString().trim()
                    : "";

            // Validasi email
            if (TextUtils.isEmpty(email)) {
                editTextForgot.setError("Email wajib diisi");
                editTextForgot.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextForgot.setError("Format email tidak valid");
                editTextForgot.requestFocus();
                return;
            }

            // Karena Firebase sudah tidak digunakan,
            // kita hanya tampilkan pesan sementara.
            Toast.makeText(ForgotPasswordActivity.this,
                    "Fitur reset password belum terhubung ke server.",
                    Toast.LENGTH_LONG).show();

            // Kembali ke login
            startActivity(new Intent(ForgotPasswordActivity.this, loginactivity.class));
            finish();
        });
    }
}
