package com.example.myapplication;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
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
        btnBack.setOnClickListener(v -> finish());

        ImageView ivHero = findViewById(R.id.ivHero);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvNotes = findViewById(R.id.tvNotes);
        TextView tvPairing = findViewById(R.id.tvPairing);
        TextView tvQuantity = findViewById(R.id.tvQuantity);
        Button btnEditQuantity = findViewById(R.id.btnEditQuantity);

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
        tvQuantity.setText(String.valueOf(wine.getQuantity() != null ? wine.getQuantity() : 0));

        btnEditQuantity.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(wine.getQuantity() != null ? wine.getQuantity() : 0));
            new AlertDialog.Builder(this)
                .setTitle(R.string.edit_quantity_title)
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    int newQuantity = 0;
                    try { newQuantity = Integer.parseInt(input.getText().toString()); } catch (Exception ignore) {}
                    wine.setQuantity(newQuantity);
                    tvQuantity.setText(String.valueOf(newQuantity));
                    WineRepository repo1 = new WineRepository(this);
                    repo1.update(wine);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });

        findViewById(R.id.btnAddToOrder).setOnClickListener(v -> {
            if (wine.getQuantity() == null || wine.getQuantity() <= 0) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.stock_unavailable_title)
                        .setMessage(R.string.stock_unavailable_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }
            android.content.Intent intent = new android.content.Intent(this, OrdersActivity.class);
            intent.putExtra(OrdersActivity.EXTRA_PRESELECTED_WINE_ID, wine.getId());
            startActivity(intent);
        });
    }
}
