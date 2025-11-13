package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        EditText emailEt = findViewById(R.id.etEmail);
        EditText passwordEt = findViewById(R.id.etPassword);
        Button loginBtn = findViewById(R.id.btnLogin);
        TextView registerTv = findViewById(R.id.tvRegister);

        loginBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            // Validação: campos obrigatórios
            if (TextUtils.isEmpty(email)) {
                emailEt.setError("Preencha o e-mail");
                emailEt.requestFocus();
                return;
            }

            // Validação: formato de e-mail
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEt.setError("Formato de e-mail inválido");
                emailEt.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEt.setError("Preencha a senha");
                passwordEt.requestFocus();
                return;
            }

            // Validação: comprimento mínimo da senha
            if (password.length() < 7) {
                passwordEt.setError("A senha deve ter ao menos 7 caracteres");
                passwordEt.requestFocus();
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
