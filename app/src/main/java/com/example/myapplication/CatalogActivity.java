package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.Wine;
import com.example.myapplication.data.repository.WineRepository;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    private WineRepository repo;
    private WinesAdapter adapter;

    // Image picker state for the add dialog
    private Uri pendingImageUri = null;
    private ImageView pendingPreview = null;
    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);} catch (Exception ignore) {}
                    pendingImageUri = uri;
                    if (pendingPreview != null) {
                        pendingPreview.setImageURI(uri);
                        pendingPreview.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        repo = new WineRepository(this);

        // Top bar elements
        EditText etSearch = findViewById(R.id.etSearch);
        Spinner spType = findViewById(R.id.spType);
        ImageButton btnAdd = findViewById(R.id.btnAdd);

        // Filters spinner
        String[] types = new String[]{"Todos os tipos", "Tinto", "Branco", "Rosé", "Espumante"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(spAdapter);

        // RecyclerView dos vinhos (grade 2 colunas)
        RecyclerView rv = findViewById(R.id.rvWines);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rv.setLayoutManager(glm);
        rv.addItemDecoration(new SpacingDecoration(8)); // 8dp spacing
        adapter = new WinesAdapter();
        adapter.attachRepo(repo);
        rv.setAdapter(adapter);

        // Carrega dados
        refreshList();

        // Busca por nome
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String t = (String) parent.getItemAtPosition(position);
                adapter.setTypeFilter("Todos os tipos".equals(t) ? null : t);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { adapter.setTypeFilter(null); }
        });

        // Add wine dialog
        btnAdd.setOnClickListener(v -> openAddDialog());

        // Bottom carousel like Home
        RecyclerView bottomNav = findViewById(R.id.bottomNav);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bottomNav.setLayoutManager(lm);
        bottomNav.setItemViewCacheSize(6);
        List<BottomItem> items = new ArrayList<>();
        items.add(new BottomItem("Dashboard", R.drawable.ic_dashboard));
        items.add(new BottomItem("Catálogo", R.drawable.ic_cup_straw));
        items.add(new BottomItem("Clientes", R.drawable.ic_person));
        items.add(new BottomItem("Pedidos", R.drawable.ic_cart));
        items.add(new BottomItem("Rotas", R.drawable.ic_map));
        BottomCarouselAdapter bottomAdapter = new BottomCarouselAdapter(items, (pos, item) -> {
            Intent intent = null;
            String title = item.getTitle();
            if ("Dashboard".equals(title)) {
                intent = new Intent(this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if ("Catálogo".equals(title)) {
                return; // already here
            } else if ("Clientes".equals(title)) {
                intent = new Intent(this, ClientsActivity.class);
            } else if ("Pedidos".equals(title)) {
                intent = new Intent(this, OrdersActivity.class);
            } else if ("Rotas".equals(title)) {
                intent = new Intent(this, RoutesActivity.class);
            }
            if (intent != null) startActivity(intent);
        });
        bottomNav.setAdapter(bottomAdapter);
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(bottomNav);
        bottomAdapter.setSelectedPosition(1);
    }

    private void refreshList() {
        List<Wine> wines = repo.all();
        adapter.setData(wines);
    }

    private void openAddDialog() {
        pendingImageUri = null;
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_wine, null, false);
        EditText etName = form.findViewById(R.id.etName);
        Spinner spType = form.findViewById(R.id.spType);
        EditText etYear = form.findViewById(R.id.etYear);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etNotes = form.findViewById(R.id.etNotes);
        EditText etPairing = form.findViewById(R.id.etPairing);
        EditText etQuantity = form.findViewById(R.id.etQuantity);
        View pickArea = form.findViewById(R.id.pickImageArea);
        pendingPreview = form.findViewById(R.id.ivPreview);

        String[] types = new String[]{"Tinto", "Branco", "Rosé", "Espumante"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        pickArea.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_add_wine)
                .setView(form)
                .setPositiveButton(R.string.dialog_save, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String type = spType.getSelectedItem() != null ? spType.getSelectedItem().toString() : null;
                    Integer year = etYear.getText().length() > 0 ? Integer.valueOf(etYear.getText().toString()) : null;
                    Double price = etPrice.getText().length() > 0 ? Double.valueOf(etPrice.getText().toString().replace(",", ".")) : null;
                    String notes = etNotes.getText().toString();
                    String pairing = etPairing.getText().toString();
                    String image = pendingImageUri != null ? pendingImageUri.toString() : null;
                    Integer quantity = etQuantity.getText().length() > 0 ? Integer.valueOf(etQuantity.getText().toString()) : 0;

                    if (name.isEmpty()) return; // minimal validation
                    Wine wine = new Wine(null, name, type, year, price, notes, pairing, image, quantity);
                    repo.add(wine);
                    refreshList();
                    pendingImageUri = null;
                    pendingPreview = null;
                })
                .setNegativeButton(R.string.dialog_cancel, (d, which) -> {
                    pendingImageUri = null;
                    pendingPreview = null;
                })
                .show();
    }

    // Simple item decoration to add spacing in grid
    static class SpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spacePx;
        SpacingDecoration(int dp) { this.spacePx = (int) (dp * Resources.getSystem().getDisplayMetrics().density); }
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            outRect.top = spacePx;
            outRect.bottom = spacePx;
            if (pos % 2 == 0) { // left column
                outRect.left = spacePx;
                outRect.right = spacePx / 2;
            } else { // right column
                outRect.left = spacePx / 2;
                outRect.right = spacePx;
            }
        }
    }

    // Adapter interno simples para listagem
    static class WinesAdapter extends RecyclerView.Adapter<WinesAdapter.VH> {
        private final List<Wine> all = new ArrayList<>();
        private final List<Wine> visible = new ArrayList<>();
        private String query = "";
        private String typeFilter = null;
        private WineRepository repo; // referência para exclusão

        void attachRepo(WineRepository r) { this.repo = r; }

        void setData(List<Wine> data) {
            all.clear();
            if (data != null) all.addAll(data);
            applyFilters();
        }

        void setQuery(String q) { this.query = q == null ? "" : q; applyFilters(); }
        void setTypeFilter(String t) { this.typeFilter = t; applyFilters(); }

        private void applyFilters() {
            visible.clear();
            String qq = query.toLowerCase();
            for (Wine w : all) {
                boolean okName = w.getName() != null && w.getName().toLowerCase().contains(qq);
                boolean okType = typeFilter == null || (w.getType() != null && w.getType().equalsIgnoreCase(typeFilter));
                if (okName && okType) visible.add(w);
            }
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wine_card, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Wine w = visible.get(position);
            ImageView iv = h.itemView.findViewById(R.id.ivWine);
            if (w.getImageUri() != null) {
                try { iv.setImageURI(Uri.parse(w.getImageUri())); } catch (Exception ignore) {}
            } else { iv.setImageResource(R.drawable.vinho); }
            TextView tvType = h.itemView.findViewById(R.id.tvWineType);
            TextView tvYear = h.itemView.findViewById(R.id.tvWineYear);
            TextView tvName = h.itemView.findViewById(R.id.tvWineName);
            TextView tvPrice = h.itemView.findViewById(R.id.tvWinePrice);
            TextView tvQty = h.itemView.findViewById(R.id.tvWineQuantity);
            tvType.setText(w.getType() != null ? w.getType() : "");
            tvYear.setText(w.getYear() != null ? "Safra " + w.getYear() : "");
            tvName.setText(w.getName() != null ? w.getName() : "");
            tvPrice.setText(w.getPrice() != null ? String.format("R$ %.2f", w.getPrice()) : "");
            tvQty.setText("Estoque: " + (w.getQuantity() != null ? w.getQuantity() : 0));
            ImageButton btnDelete = h.itemView.findViewById(R.id.btnDeleteWine);
            btnDelete.setOnClickListener(v -> {
                if (repo == null || w.getId() == null) return;
                new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.delete_wine_title)
                        .setMessage(v.getContext().getString(R.string.delete_wine_confirm, w.getName()))
                        .setPositiveButton(R.string.action_delete, (d, which) -> {
                            repo.delete(w.getId());
                            all.remove(w);
                            applyFilters();
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            });
            h.itemView.setOnClickListener(v -> {
                Context ctx = v.getContext();
                Intent it = new Intent(ctx, WineDetailsActivity.class);
                it.putExtra(WineDetailsActivity.EXTRA_WINE_ID, w.getId());
                ctx.startActivity(it);
            });
        }
        @Override public int getItemCount() { return visible.size(); }
        static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
    }
}
