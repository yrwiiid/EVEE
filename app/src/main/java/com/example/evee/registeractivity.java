package com.example.evee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

        // Validasi
        if (TextUtils.isEmpty(name)) { edtName.setError("Nama harus diisi"); return; }
        if (TextUtils.isEmpty(email)) { edtEmail.setError("Email harus diisi"); return; }
        if (TextUtils.isEmpty(password)) { edtPassword.setError("Password harus diisi"); return; }
        if (!password.equals(confirmPassword)) { edtConfirmPassword.setError("Password tidak sama"); return; }

        String url = ApiConfig.REGISTER_URL;
        Log.e("REGISTER_DEBUG", "API URL = " + url);

        // Build JSON body
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
        } catch (Exception ignored) {}

        Log.e("REGISTER_DEBUG", "JSON Body = " + body.toString());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.e("REGISTER_DEBUG", "Response = " + response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {

                            JSONObject userObj = obj.getJSONObject("user");

                            SharedPreferences sp = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("id", userObj.getString("id"));
                            editor.putString("name", userObj.getString("name"));
                            editor.putString("email", userObj.getString("email"));
                            editor.apply();

                            Toast.makeText(this, "Register berhasil!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(registeractivity.this, loginactivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, obj.getString("error"), Toast.LENGTH_SHORT).show();
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
                    } else {
                        Log.e("REGISTER_DEBUG", "Network Response NULL (masalah koneksi/ngrok)");
                    }

                    Toast.makeText(this, "Error koneksi", Toast.LENGTH_SHORT).show();
                }
        ) {

            // SEND JSON BODY
            @Override
            public byte[] getBody() {
                return body.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            // SEND JSON HEADERS
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // FIX: Ngrok suka block retry → OFF
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }
}
