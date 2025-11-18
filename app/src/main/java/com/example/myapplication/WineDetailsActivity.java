package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.model.Wine;
import com.example.myapplication.data.repository.WineRepository;

import java.util.Locale;

public class WineDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_WINE_ID = "extra_wine_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wine_details);

        long id = getIntent().getLongExtra(EXTRA_WINE_ID, -1L);
        if (id == -1L) { finish(); return; }

        WineRepository repo = new WineRepository(this);
        Wine wine = repo.get(id);
        if (wine == null) { finish(); return; }

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView ivHero = findViewById(R.id.ivHero);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvNotes = findViewById(R.id.tvNotes);
        TextView tvPairing = findViewById(R.id.tvPairing);

        if (wine.getImageUri() != null) {
            try { ivHero.setImageURI(Uri.parse(wine.getImageUri())); } catch (Exception ignore) {}
        } else {
            ivHero.setImageResource(R.drawable.vinho);
        }
        tvType.setText(wine.getType() != null ? wine.getType() : "");
        tvName.setText(wine.getName());
        tvYear.setText(wine.getYear() != null ? "Safra " + wine.getYear() : "");
        tvPrice.setText(wine.getPrice() != null ? String.format(Locale.getDefault(), "R$ %.2f", wine.getPrice()) : "");
        tvNotes.setText(wine.getNotes() != null ? wine.getNotes() : getString(R.string.em_dash));
        tvPairing.setText(wine.getPairing() != null ? wine.getPairing() : getString(R.string.em_dash));

        findViewById(R.id.btnAddToOrder).setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 600);
        });
    }
}
