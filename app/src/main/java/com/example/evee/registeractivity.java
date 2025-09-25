package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registeractivity extends AppCompatActivity {

    private TextInputEditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton buttonRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

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
            finish(); // supaya tidak bisa balik ke register dengan tombol back
        });

        // Firebase Auth & Database
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://evee-aee6e-default-rtdb.firebaseio.com/");

        // Tombol register
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

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

        // Buat user dengan Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update profile FirebaseAuth (set display name)
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                            firebaseUser.updateProfile(profileUpdates);

                            // Simpan ke Realtime Database (Users/UID)
                            String userId = firebaseUser.getUid();
                            User user = new User(name, email);

                            dbRef.child("Users").child(userId).setValue(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(registeractivity.this,
                                                "Register berhasil! Silakan login.",
                                                Toast.LENGTH_SHORT).show();

                                        // Pindah ke login
                                        startActivity(new Intent(registeractivity.this, loginactivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(registeractivity.this,
                                                "Gagal simpan data: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(registeractivity.this,
                                "Register gagal: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Model User (nama + email)
    public static class User {
        public String name;
        public String email;

        public User() {
            // Default constructor dibutuhkan Firebase
        }

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
