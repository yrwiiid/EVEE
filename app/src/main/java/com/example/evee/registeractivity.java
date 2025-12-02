package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class registeractivity extends AppCompatActivity {

    private TextInputEditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        String url = ApiConfig.REGISTER_URL;
        Log.e("REGISTER_DEBUG", "API URL = " + url);

        // Build JSON body
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Gunakan JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Log.e("REGISTER_DEBUG", "Response = " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject userObj = response.getJSONObject("user");

                            // Simpan user ke SessionManager
                            SessionManager sessionManager = new SessionManager(this);
                            sessionManager.saveUser(
                                    userObj.getString("id"),
                                    userObj.getString("name"),
                                    userObj.getString("email")
                            );

                            Toast.makeText(this, "Register berhasil!", Toast.LENGTH_SHORT).show();

                            // Arahkan ke MenstruasiActivity
                            Intent intent = new Intent(registeractivity.this, MenstruasiActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("REGISTER_DEBUG", "JSON parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("REGISTER_DEBUG", "Volley Error = " + error.toString());
                    if (error.networkResponse != null) {
                        String err = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("REGISTER_DEBUG", "HTTP CODE = " + error.networkResponse.statusCode);
                        Log.e("REGISTER_DEBUG", "Error Body = " + err);
                    }
                    Toast.makeText(this, "Error koneksi", Toast.LENGTH_SHORT).show();
                }
        );

        // Retry policy (ngrok kadang block retry)
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }
}
