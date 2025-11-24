package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.Client;
import com.example.myapplication.data.repository.ClientRepository;
import com.example.myapplication.util.BrDocumentMask;
import com.example.myapplication.util.BrPhoneMask;

import java.util.ArrayList;
import java.util.List;

public class ClientsActivity extends AppCompatActivity {
    private ClientRepository repo;
    private ClientsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);
        setTitle("Clientes");

        repo = new ClientRepository(this);

        EditText etSearch = findViewById(R.id.etSearchClients);
        ImageButton btnAdd = findViewById(R.id.btnAddClient);

        RecyclerView rv = findViewById(R.id.rvClients);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // Instantiate adapter with click listener to open the client detail modal
        adapter = new ClientsAdapter(this::onClientClicked);
        rv.setAdapter(adapter);

        refreshList();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.setQuery(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v -> openAddDialog());

        // Bottom carousel similar to catalog
        RecyclerView bottomNav = findViewById(R.id.bottomNavClients);
        bottomNav.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
                intent = new Intent(this, CatalogActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if ("Clientes".equals(title)) {
                return; // already here
            } else if ("Pedidos".equals(title)) {
                intent = new Intent(this, OrdersActivity.class);
            } else if ("Rotas".equals(title)) {
                intent = new Intent(this, RoutesActivity.class);
            }
            if (intent != null) startActivity(intent);
        });
        bottomNav.setAdapter(bottomAdapter);
        new LinearSnapHelper().attachToRecyclerView(bottomNav);
        bottomAdapter.setSelectedPosition(2);
    }

    private void refreshList() {
        adapter.setData(repo.all());
    }

    private void openAddDialog() {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_client, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDocument = form.findViewById(R.id.etDocument);
        EditText etAddress = form.findViewById(R.id.etAddress);
        EditText etResponsible = form.findViewById(R.id.etResponsible);
        EditText etPhone = form.findViewById(R.id.etPhone);

        BrDocumentMask docMask = new BrDocumentMask(etDocument);
        etDocument.addTextChangedListener(docMask);
        BrPhoneMask phoneMask = new BrPhoneMask(etPhone);
        etPhone.addTextChangedListener(phoneMask);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.new_client_title)
                .setView(form)
                .setPositiveButton(R.string.dialog_save, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
        dlg.setOnShowListener(l -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                boolean ok = true;
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { etName.setError(getString(R.string.error_client_name_required)); ok = false; }
                String addr = etAddress.getText().toString().trim();
                if (addr.isEmpty()) { etAddress.setError(getString(R.string.error_client_address_required)); ok = false; }
                String resp = etResponsible.getText().toString().trim();
                if (resp.isEmpty()) { etResponsible.setError(getString(R.string.error_client_responsible_required)); ok = false; }
                if (!docMask.isValid()) { etDocument.setError(getString(R.string.error_client_document_invalid)); ok = false; }
                if (!phoneMask.isValid()) { etPhone.setError(getString(R.string.error_client_phone_invalid)); ok = false; }
                if (!ok) return;
                String doc = etDocument.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                repo.add(new Client(null, name, doc, addr, resp, phone));
                refreshList();
                dlg.dismiss();
            });
        });
        dlg.show();
    }

    // Open the client detail dialog with Edit/Delete actions
    private void onClientClicked(@NonNull Client c) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_client_detail, null, false);
        ((TextView) view.findViewById(R.id.tvClientName)).setText(c.getName() != null ? c.getName() : "");
        ((TextView) view.findViewById(R.id.tvClientDocument)).setText(c.getDocument() != null ? c.getDocument() : "");
        ((TextView) view.findViewById(R.id.tvClientAddress)).setText(c.getAddress() != null ? c.getAddress() : "");
        ((TextView) view.findViewById(R.id.tvClientResponsible)).setText(c.getResponsible() != null ? c.getResponsible() : "");
        ((TextView) view.findViewById(R.id.tvClientPhone)).setText(c.getPhone() != null ? c.getPhone() : "");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        view.findViewById(R.id.btnEditClient).setOnClickListener(v -> {
            dialog.dismiss();
            openEditDialog(c);
        });

        view.findViewById(R.id.btnDeleteClient).setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_client_title)
                    .setMessage(getString(R.string.delete_client_confirm, c.getName() != null ? c.getName() : ""))
                    .setPositiveButton(R.string.dialog_delete, (d, w) -> {
                        Long id = c.getId();
                        if (id != null) {
                            repo.delete(id);
                            refreshList();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        });

        dialog.show();
    }

    private void openEditDialog(@NonNull Client c) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_client, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDocument = form.findViewById(R.id.etDocument);
        EditText etAddress = form.findViewById(R.id.etAddress);
        EditText etResponsible = form.findViewById(R.id.etResponsible);
        EditText etPhone = form.findViewById(R.id.etPhone);

        etName.setText(c.getName());
        etDocument.setText(c.getDocument());
        etAddress.setText(c.getAddress());
        etResponsible.setText(c.getResponsible());
        etPhone.setText(c.getPhone());

        BrDocumentMask docMask = new BrDocumentMask(etDocument);
        etDocument.addTextChangedListener(docMask);
        BrPhoneMask phoneMask = new BrPhoneMask(etPhone);
        etPhone.addTextChangedListener(phoneMask);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_client_title)
                .setView(form)
                .setPositiveButton(R.string.dialog_save, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
        dlg.setOnShowListener(l -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                boolean ok = true;
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { etName.setError(getString(R.string.error_client_name_required)); ok = false; }
                String addr = etAddress.getText().toString().trim();
                if (addr.isEmpty()) { etAddress.setError(getString(R.string.error_client_address_required)); ok = false; }
                String resp = etResponsible.getText().toString().trim();
                if (resp.isEmpty()) { etResponsible.setError(getString(R.string.error_client_responsible_required)); ok = false; }
                if (!docMask.isValid()) { etDocument.setError(getString(R.string.error_client_document_invalid)); ok = false; }
                if (!phoneMask.isValid()) { etPhone.setError(getString(R.string.error_client_phone_invalid)); ok = false; }
                if (!ok) return;
                String doc = etDocument.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                Client updated = new Client(c.getId(), name, doc, addr, resp, phone);
                repo.update(updated);
                refreshList();
                dlg.dismiss();
            });
        });
        dlg.show();
    }

    static class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.VH> {
        interface OnClientClickListener { void onClientClick(Client c); }

        private final List<Client> all = new ArrayList<>();
        private final List<Client> visible = new ArrayList<>();
        private String query = "";
        private final OnClientClickListener clickListener;

        ClientsAdapter(@NonNull OnClientClickListener clickListener) {
            this.clickListener = clickListener;
        }

        void setData(List<Client> data) {
            all.clear();
            if (data != null) all.addAll(data);
            applyFilters();
        }
        void setQuery(String q) { this.query = q == null ? "" : q; applyFilters(); }

        private void applyFilters() {
            visible.clear();
            String qq = query.toLowerCase();
            for (Client c : all) {
                boolean okName = c.getName() != null && c.getName().toLowerCase().contains(qq);
                boolean okDoc = c.getDocument() != null && c.getDocument().toLowerCase().contains(qq);
                if (okName || okDoc) visible.add(c);
            }
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_card, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Client c = visible.get(position);
            ((TextView) h.itemView.findViewById(R.id.tvClientName)).setText(c.getName());
            ((TextView) h.itemView.findViewById(R.id.tvClientDocument)).setText(c.getDocument() != null ? c.getDocument() : "");
            ((TextView) h.itemView.findViewById(R.id.tvClientAddress)).setText(c.getAddress() != null ? c.getAddress() : "");
            ((TextView) h.itemView.findViewById(R.id.tvClientResponsible)).setText(c.getResponsible() != null ? c.getResponsible() : "");
            ((TextView) h.itemView.findViewById(R.id.tvClientPhone)).setText(c.getPhone() != null ? c.getPhone() : "");

            h.itemView.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onClientClick(visible.get(pos));
                }
            });
        }
        @Override public int getItemCount() { return visible.size(); }
        static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
    }
}
