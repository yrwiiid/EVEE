package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class registeractivity extends AppCompatActivity {

    private TextInputEditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inisialisasi View
        edtName = findViewById(R.id.editTextName);
        edtEmail = findViewById(R.id.editTextEmail);
        edtPassword = findViewById(R.id.editTextPassword);
        edtConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        TextView textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(registeractivity.this, loginactivity.class));
            finish();
        });

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

        // Validasi input
        if (TextUtils.isEmpty(name)) {
            edtName.setError("Nama harus diisi");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email harus diisi");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password harus diisi");
            return;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Password tidak sama");
            return;
        }

        // Karena tidak ada Firebase & tidak ada Retrofit,
        // cukup tampilkan toast sukses sementara
        Toast.makeText(this,
                "Validasi berhasil! (Belum tersambung ke server)",
                Toast.LENGTH_SHORT).show();

        // Pindah ke halaman selanjutnya
        Intent i = new Intent(registeractivity.this, MenstruasiActivity.class);
        startActivity(i);
        finish();
    }
}
