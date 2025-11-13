package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String email = getIntent().getStringExtra("email");
        TextView tv = findViewById(R.id.tvWelcome);
        tv.setText("Bem-vindo, " + (email != null ? email : "usuário"));
    }
}

