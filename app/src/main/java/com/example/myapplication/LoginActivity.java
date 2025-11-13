package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt;
    private Button loginBtn;
    private TextView registerTv;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        emailEt = findViewById(R.id.etEmail);
        passwordEt = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        registerTv = findViewById(R.id.tvRegister);

        loginBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkUser(email, password)) {
                // login successful
                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                i.putExtra("email", email);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Credenciais inválidas", Toast.LENGTH_SHORT).show();
            }
        });

        registerTv.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }
}

