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

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        EditText emailEt = findViewById(R.id.etEmail);
        EditText passwordEt = findViewById(R.id.etPassword);
        Button registerBtn = findViewById(R.id.btnRegister);
        TextView backTv = findViewById(R.id.tvBackLogin);

        registerBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEt.setError("Preencha o e-mail");
                emailEt.requestFocus();
                return;
            }

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

            if (password.length() < 7) {
                passwordEt.setError("A senha deve ter ao menos 7 caracteres");
                passwordEt.requestFocus();
                return;
            }

            if (db.checkUserExists(email)) {
                Toast.makeText(RegisterActivity.this, "Usuário já existe", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean added = db.addUser(email, password);
            if (added) {
                Toast.makeText(RegisterActivity.this, "Registro criado com sucesso", Toast.LENGTH_SHORT).show();
                // volta para login
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Erro ao criar usuário", Toast.LENGTH_SHORT).show();
            }
        });

        backTv.setOnClickListener(v -> {
            // apenas fecha a activity para voltar ao LoginActivity
            finish();
        });
    }
}
