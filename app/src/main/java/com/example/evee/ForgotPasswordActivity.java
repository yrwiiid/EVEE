    package com.example.evee;

    import android.content.Intent;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.util.Patterns;
    import android.widget.Button;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.textfield.TextInputEditText;
    import com.google.firebase.auth.FirebaseAuth;

    public class ForgotPasswordActivity extends AppCompatActivity {

        private TextInputEditText editTextForgot;
        private Button buttonResetPassword;
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.lupa_password);

            editTextForgot = findViewById(R.id.editTextForgot);
            buttonResetPassword = findViewById(R.id.buttonResetPassword);
            mAuth = FirebaseAuth.getInstance();

            buttonResetPassword.setOnClickListener(v -> {
                String email = editTextForgot.getText().toString().trim();

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

                // Kirim email reset password via Firebase
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Link reset dikirim ke " + email,
                                        Toast.LENGTH_LONG).show();
                                finish(); // kembali ke login
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Gagal mengirim reset: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                            startActivity(new Intent(this, loginactivity.class));

                        });
            });
        }
    }
