package com.example.myapplication;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
        View btnEditWine = findViewById(R.id.btnEditWine);

        if (wine.getImageUri() != null) {
            try { ivHero.setImageURI(Uri.parse(wine.getImageUri())); } catch (Exception ignore) {}
        } else {
            ivHero.setImageResource(R.drawable.vinho);
        }
        tvType.setText(wine.getType() != null ? wine.getType() : "");
        tvName.setText(wine.getName());
        tvYear.setText(wine.getYear() != null ? getString(R.string.wine_year_format, wine.getYear()) : "");
        tvPrice.setText(wine.getPrice() != null ? getString(R.string.wine_price_format, wine.getPrice()) : "");
        tvNotes.setText(wine.getNotes() != null && !wine.getNotes().isEmpty() ? wine.getNotes() : getString(R.string.em_dash));
        tvPairing.setText(wine.getPairing() != null && !wine.getPairing().isEmpty() ? wine.getPairing() : getString(R.string.em_dash));
        tvQuantity.setText(String.valueOf(wine.getQuantity() != null ? wine.getQuantity() : 0));

        btnEditWine.setOnClickListener(v -> openEditWineDialog(wine, tvQuantity));

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

    private void openEditWineDialog(Wine wine, TextView tvQuantity) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_wine, null, false);
        EditText etName = form.findViewById(R.id.etName);
        Spinner spType = form.findViewById(R.id.spType);
        EditText etYear = form.findViewById(R.id.etYear);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etNotes = form.findViewById(R.id.etNotes);
        EditText etPairing = form.findViewById(R.id.etPairing);
        EditText etQuantity = form.findViewById(R.id.etQuantity);
        ImageView ivPreview = form.findViewById(R.id.ivPreview);
        View pickArea = form.findViewById(R.id.pickImageArea);

        String[] types = new String[]{"Tinto", "Branco", "Rosé", "Espumante"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        etName.setText(wine.getName());
        if (wine.getType() != null) {
            for (int i = 0; i < types.length; i++) if (types[i].equalsIgnoreCase(wine.getType())) { spType.setSelection(i); break; }
        }
        etYear.setText(wine.getYear() != null ? String.valueOf(wine.getYear()) : "");
        etPrice.setText(wine.getPrice() != null ? String.format(Locale.getDefault(), "%.2f", wine.getPrice()) : "");
        etNotes.setText(wine.getNotes());
        etPairing.setText(wine.getPairing());
        etQuantity.setText(wine.getQuantity() != null ? String.valueOf(wine.getQuantity()) : "0");
        if (wine.getImageUri() != null) {
            try { ivPreview.setImageURI(Uri.parse(wine.getImageUri())); ivPreview.setVisibility(View.VISIBLE);} catch (Exception ignore) {}
        }
        pickArea.setOnClickListener(v -> {/* seleção de nova imagem pode ser implementada */});

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_wine_title)
                .setView(form)
                .setPositiveButton(R.string.save_wine_changes, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        dlg.setOnShowListener(l -> {
            Button btn = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                boolean ok = true;
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { etName.setError(getString(R.string.error_required)); ok = false; }
                else if (name.length() < 3) { etName.setError(getString(R.string.error_min_length_name)); ok = false; }

                Integer year = null;
                if (etYear.getText().length() > 0) {
                    try { int y = Integer.parseInt(etYear.getText().toString()); if (y < 1900 || y > 2100) { etYear.setError(getString(R.string.error_invalid_year)); ok = false; } else year = y; }
                    catch (Exception e) { etYear.setError(getString(R.string.error_invalid_year)); ok = false; }
                }

                Double price = null;
                if (etPrice.getText().length() > 0) {
                    try { double p = Double.parseDouble(etPrice.getText().toString().replace(",",".")); if (p <= 0) { etPrice.setError(getString(R.string.error_invalid_price)); ok = false; } else price = p; }
                    catch (Exception e) { etPrice.setError(getString(R.string.error_invalid_price)); ok = false; }
                }

                Integer quantity = 0;
                if (etQuantity.getText().length() > 0) {
                    try { int q = Integer.parseInt(etQuantity.getText().toString()); if (q < 0) { etQuantity.setError(getString(R.string.error_invalid_quantity)); ok = false; } else quantity = q; }
                    catch (Exception e) { etQuantity.setError(getString(R.string.error_invalid_quantity)); ok = false; }
                }

                if (!ok) return;

                wine.setName(name);
                wine.setType(spType.getSelectedItem() != null ? spType.getSelectedItem().toString() : wine.getType());
                wine.setYear(year);
                wine.setPrice(price);
                wine.setNotes(etNotes.getText().toString());
                wine.setPairing(etPairing.getText().toString());
                wine.setQuantity(quantity);
                new WineRepository(this).update(wine);

                ((TextView) findViewById(R.id.tvType)).setText(wine.getType());
                ((TextView) findViewById(R.id.tvName)).setText(wine.getName());
                ((TextView) findViewById(R.id.tvYear)).setText(wine.getYear()!=null? getString(R.string.wine_year_format, wine.getYear()):"");
                ((TextView) findViewById(R.id.tvPrice)).setText(wine.getPrice()!=null? getString(R.string.wine_price_format, wine.getPrice()):"");
                ((TextView) findViewById(R.id.tvNotes)).setText(wine.getNotes()!=null && !wine.getNotes().isEmpty()? wine.getNotes(): getString(R.string.em_dash));
                ((TextView) findViewById(R.id.tvPairing)).setText(wine.getPairing()!=null && !wine.getPairing().isEmpty()? wine.getPairing(): getString(R.string.em_dash));
                tvQuantity.setText(String.valueOf(wine.getQuantity()!=null? wine.getQuantity():0));
                dlg.dismiss();
            });
        });

        dlg.show();
    }
}
