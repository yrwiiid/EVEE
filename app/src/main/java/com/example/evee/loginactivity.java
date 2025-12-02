package com.example.evee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class loginactivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView registerLink, forgotPasswordLink;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.buttonLogin);
        registerLink = findViewById(R.id.textViewRegister);
        forgotPasswordLink = findViewById(R.id.textViewForgotPassword);

        // Sudah login, langsung ke dashboard
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, dashboard.class));
            finish();
        }

        loginBtn.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(loginactivity.this, registeractivity.class))
        );
        forgotPasswordLink.setOnClickListener(v ->
                startActivity(new Intent(loginactivity.this, ForgotPasswordActivity.class))
        );
    }

    private void loginUser() {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (emailText.isEmpty()) { email.setError("Email harus diisi"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) { email.setError("Format email tidak valid"); return; }
        if (passwordText.isEmpty()) { password.setError("Password harus diisi"); return; }

        String url = ApiConfig.LOGIN_URL;

        JSONObject body = new JSONObject();
        try {
            body.put("email", emailText);
            body.put("password", passwordText);
        } catch (Exception ignored) {}

        Log.e("LOGIN_DEBUG", "URL = " + url);
        Log.e("LOGIN_DEBUG", "Body = " + body.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Log.e("LOGIN_DEBUG", "Response = " + response);

                    try {
                        if (response.getBoolean("success")) {

                            JSONObject user = response.getJSONObject("user");

                            sessionManager.saveUser(
                                    user.getString("id"),
                                    user.getString("name"),
                                    user.getString("email")
                            );

                            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, dashboard.class));
                            finish();
                        } else {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("LOGIN_DEBUG", "JSON error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("LOGIN_DEBUG", "Volley Error = " + error.toString());

                    if (error.networkResponse != null) {
                        String err = new String(error.networkResponse.data);
                        Log.e("LOGIN_DEBUG", "HTTP CODE = " + error.networkResponse.statusCode);
                        Log.e("LOGIN_DEBUG", "Error Body = " + err);
                    }

                    Toast.makeText(this, "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        password.setText("");
    }
}
