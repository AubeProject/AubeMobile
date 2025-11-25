package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout; // added
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.Client;
import com.example.myapplication.data.model.Wine;
import com.example.myapplication.data.repository.ClientRepository;
import com.example.myapplication.data.dao.WineDao;
import com.example.myapplication.data.dao.impl.WineDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.repository.OrderRepository;
import com.example.myapplication.data.model.Order;
import com.example.myapplication.data.model.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {
    public static final String EXTRA_PRESELECTED_WINE_ID = "extra_preselected_wine_id";

    private RecyclerView rvOrders;
    private OrdersAdapter adapter;
    private EditText etSearch;
    private ImageButton btnAdd;

    private final List<Order> allOrders = new ArrayList<>();
    private ClientRepository clientRepo;
    private WineDao wineDao;
    private OrderRepository orderRepo;

    // --- Status mapping helpers (internal codes vs labels mostrados) ---
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_DRAFT = "DRAFT";

    private String[] getStatusCodes() {
        return new String[]{STATUS_PENDING, STATUS_DELIVERED, STATUS_CANCELLED, STATUS_DRAFT};
    }

    private String[] getStatusLabels() {
        return new String[]{
                getString(R.string.status_pending),
                getString(R.string.status_delivered),
                getString(R.string.status_cancelled),
                getString(R.string.status_draft)
        };
    }

    private String getStatusLabelByCode(String code) {
        if (STATUS_DELIVERED.equals(code)) return getString(R.string.status_delivered);
        if (STATUS_CANCELLED.equals(code)) return getString(R.string.status_cancelled);
        if (STATUS_DRAFT.equals(code)) return getString(R.string.status_draft);
        return getString(R.string.status_pending);
    }

    private String getStatusCodeByLabel(String label) {
        if (label == null) return STATUS_PENDING;
        if (label.equals(getString(R.string.status_delivered))) return STATUS_DELIVERED;
        if (label.equals(getString(R.string.status_cancelled))) return STATUS_CANCELLED;
        if (label.equals(getString(R.string.status_draft))) return STATUS_DRAFT;
        if (label.equals(getString(R.string.status_pending))) return STATUS_PENDING;
        // Caso dados antigos já estejam salvos com código, retorna direto
        if (Arrays.asList(getStatusCodes()).contains(label)) return label;
        return STATUS_PENDING;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        setTitle("Pedidos");

        clientRepo = new ClientRepository(this);
        wineDao = new WineDaoImpl(AppDatabase.getInstance(this));
        orderRepo = new OrderRepository(this);

        rvOrders = findViewById(R.id.rvOrders);
        etSearch = findViewById(R.id.etSearchOrders);
        btnAdd = findViewById(R.id.btnAddOrder);

        adapter = new OrdersAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        // Bottom navigation setup
        RecyclerView bottomNav = findViewById(R.id.bottomNavOrders);
        bottomNav.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<BottomItem> items = new ArrayList<>();
        items.add(new BottomItem("Dashboard", R.drawable.ic_dashboard));
        items.add(new BottomItem("Catálogo", R.drawable.ic_cup_straw));
        items.add(new BottomItem("Clientes", R.drawable.ic_person));
        items.add(new BottomItem("Pedidos", R.drawable.ic_cart));
        items.add(new BottomItem("Rotas", R.drawable.ic_map));
        BottomCarouselAdapter navAdapter = new BottomCarouselAdapter(items, (pos, item) -> {
            String title = item.getTitle();
            if ("Pedidos".equals(title)) return; // current
            if ("Dashboard".equals(title)) startActivity(new android.content.Intent(this, HomeActivity.class));
            else if ("Catálogo".equals(title)) startActivity(new android.content.Intent(this, CatalogActivity.class));
            else if ("Clientes".equals(title)) startActivity(new android.content.Intent(this, ClientsActivity.class));
            else if ("Rotas".equals(title)) startActivity(new android.content.Intent(this, RoutesActivity.class));
        });
        bottomNav.setAdapter(navAdapter);
        navAdapter.setSelectedPosition(3);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v -> openCreateOrderDialog(null));

        loadOrdersFromDb();
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().hasExtra(EXTRA_PRESELECTED_WINE_ID)) {
                long wineId = getIntent().getLongExtra(EXTRA_PRESELECTED_WINE_ID, -1L);
                if (wineId > 0) {
                    rvOrders.post(() -> openCreateOrderDialog(wineId));
                }
            }
        }
    }

    private void loadOrdersFromDb() {
        allOrders.clear();
        allOrders.addAll(orderRepo.all());
        // Normaliza status caso tenham sido salvos como texto exibido
        for (Order o : allOrders) {
            o.setStatus(getStatusCodeByLabel(o.getStatus()));
        }
        refreshList();
    }

    private void refreshList() { adapter.setData(allOrders); updateSubtitle(); }

    private void filter(String q) {
        String query = q == null ? "" : q.trim().toLowerCase();
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            String clientName = null;
            if (o.getClientId() != null) {
                Client c = clientRepo.get(o.getClientId());
                if (c != null && c.getName() != null) clientName = c.getName();
            }
            boolean matchClient = clientName != null && clientName.toLowerCase().contains(query);
            boolean matchStatus = o.getStatus() != null && getStatusLabelByCode(o.getStatus()).toLowerCase().contains(query);
            if (query.isEmpty() || matchClient || matchStatus) filtered.add(o);
        }
        adapter.setData(filtered);
    }

    private void updateSubtitle() {
        TextView sub = findViewById(R.id.tvOrdersHeaderSubtitle);
        if (sub != null) sub.setText(getString(R.string.orders_header_subtitle_format, allOrders.size()));
    }

    // Overload que permite vir da tela de catálogo com produto pré-selecionado
    public void openCreateOrderDialog(Long preselectedWineId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_order, null, false);
        Spinner spClient = dialogView.findViewById(R.id.spClient);
        Spinner spPayment = dialogView.findViewById(R.id.spPayment);
        Spinner spStatus = dialogView.findViewById(R.id.spStatus);
        Button btnAddProductRow = dialogView.findViewById(R.id.btnAddProductRow);
        ViewGroup containerProducts = dialogView.findViewById(R.id.containerProducts);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOrder);
        Button btnCreate = dialogView.findViewById(R.id.btnCreateOrder);

        List<Client> clients = clientRepo.all();
        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mapClients(clients));
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClient.setAdapter(clientAdapter);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Pagamento Pendente", "Pagamento Feito"});
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(paymentAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getStatusLabels());
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);

        btnAddProductRow.setOnClickListener(v -> addProductRow(containerProducts, null));
        addProductRow(containerProducts, preselectedWineId);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            // Validações
            int clientIndex = spClient.getSelectedItemPosition();
            if (clientIndex < 0 || clientIndex >= clients.size()) {
                Toast.makeText(this, R.string.error_order_client_required, Toast.LENGTH_SHORT).show();
                return;
            }
            List<OrderItem> items = collectItemsPersistent(containerProducts);
            if (items.isEmpty()) {
                Toast.makeText(this, R.string.error_order_need_item, Toast.LENGTH_SHORT).show();
                return;
            }
            // Verifica quantidades e estoque
            for (OrderItem oi : items) {
                Wine w = oi.getWine();
                if (oi.getQuantity() <= 0) {
                    Toast.makeText(this, R.string.error_order_invalid_quantity, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (w != null && w.getQuantity() != null && oi.getQuantity() > w.getQuantity()) {
                    Toast.makeText(this, R.string.error_order_stock_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Client selectedClient = clients.get(clientIndex);
            // Payment state
            String paymentStateLabel = (String) spPayment.getSelectedItem();
            String paymentStateCode = "PENDING";
            if ("Pagamento Feito".equals(paymentStateLabel)) paymentStateCode = "PAID";
            // Status code from spinner
            String statusLabel = (String) spStatus.getSelectedItem();
            String statusCode = getStatusCodeByLabel(statusLabel);
            Order newOrder = new Order();
            newOrder.setNumber(orderRepo.nextNumber());
            newOrder.setClientId(selectedClient.getId());
            newOrder.setStatus(statusCode);
            newOrder.setPayment(paymentStateCode);
            newOrder.setDateEpochMillis(System.currentTimeMillis());
            newOrder.setItems(items);
            orderRepo.add(newOrder);
            // Deduz estoque se entregue
            if (STATUS_DELIVERED.equals(newOrder.getStatus())) {
                for (OrderItem oi : items) {
                    Wine w = oi.getWine();
                    if (w != null && w.getQuantity() != null) {
                        int newQty = Math.max(0, w.getQuantity() - oi.getQuantity());
                        w.setQuantity(newQty);
                        wineDao.update(w);
                    }
                }
            }
            loadOrdersFromDb();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addProductRow(ViewGroup container, Long preselectedWineId) {
        List<Wine> wines = wineDao.findAll();
        if (wines.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText(R.string.no_wines_available);
            container.addView(tv);
            return;
        }
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(0,8,0,8);

        Spinner sp = new Spinner(this);
        EditText qty = new EditText(this);
        qty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        qty.setText("1");
        qty.setEms(3);
        ArrayAdapter<String> wineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mapWines(wines));
        wineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(wineAdapter);
        if (preselectedWineId != null) {
            for (int i = 0; i < wines.size(); i++) {
                Wine w = wines.get(i);
                if (w.getId() != null && w.getId().equals(preselectedWineId)) {
                    if (w.getQuantity() != null && w.getQuantity() <= 0) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.stock_unavailable_title)
                                .setMessage(R.string.stock_unavailable_message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                    sp.setSelection(i);
                    break;
                }
            }
        }
        // Botão remover
        ImageButton btnRemove = new ImageButton(this);
        btnRemove.setImageResource(R.drawable.ic_delete);
        btnRemove.setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        removeParams.setMargins(16,0,0,0);
        btnRemove.setOnClickListener(v -> {
            if (container.getChildCount() <= 1) {
                Toast.makeText(this, R.string.error_order_need_item, Toast.LENGTH_SHORT).show();
                return;
            }
            container.removeView(ll);
        });

        ll.addView(sp, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qtyParams.setMargins(16,0,0,0);
        ll.addView(qty, qtyParams);
        ll.addView(btnRemove, removeParams);
        container.addView(ll);
    }

    private List<OrderItem> collectItemsPersistent(ViewGroup container) {
        List<OrderItem> items = new ArrayList<>();
        List<Wine> wines = wineDao.findAll();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) child;
                Spinner sp = null; EditText qtyEt = null;
                for (int j = 0; j < ll.getChildCount(); j++) {
                    View inner = ll.getChildAt(j);
                    if (inner instanceof Spinner) sp = (Spinner) inner;
                    if (inner instanceof EditText) qtyEt = (EditText) inner;
                }
                if (sp != null && qtyEt != null) {
                    int wineIndex = sp.getSelectedItemPosition();
                    if (wineIndex >= 0 && wineIndex < wines.size()) {
                        Wine w = wines.get(wineIndex);
                        int qty = 1; try { qty = Integer.parseInt(qtyEt.getText().toString().trim()); } catch (Exception ignored) {}
                        if (qty <= 0) qty = 1;
                        OrderItem oi = new OrderItem(w.getId(), qty);
                        oi.setWine(w);
                        items.add(oi);
                    }
                }
            }
        }
        return items;
    }

    private String[] mapClients(List<Client> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).getName();
        return arr;
    }
    private String[] mapWines(List<Wine> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).getName();
        return arr;
    }

    private double calcTotal(Order o) {
        double sum = 0.0;
        if (o == null || o.getItems() == null) return 0.0;
        for (OrderItem it : o.getItems()) {
            if (it == null) continue;
            Wine w = it.getWine();
            if ((w == null || w.getPrice() == null) && it.getWineId() != null) {
                // fallback: fetch wine from DB
                try { w = wineDao.findById(it.getWineId()); } catch (Exception ignored) {}
            }
            Double price = (w != null ? w.getPrice() : null);
            Integer qty = it.getQuantity();
            if (price != null && qty != null) sum += price * qty;
        }
        return sum;
    }

    // --- Adapter ---
    class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {
        private final List<Order> visible = new ArrayList<>();
        void setData(List<Order> data) { visible.clear(); if (data != null) visible.addAll(data); notifyDataSetChanged(); }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Order o = visible.get(position);
            ((TextView) h.itemView.findViewById(R.id.tvOrderTitle)).setText(getString(R.string.order_title_card_format, o.getNumber()));
            TextView statusView = h.itemView.findViewById(R.id.tvOrderStatus);
            statusView.setText(getStatusLabelByCode(o.getStatus()));
            statusView.setBackgroundResource(mapStatusBg(o.getStatus()));
            String clientText = getString(R.string.em_dash);
            if (o.getClientId() != null) {
                Client c = clientRepo.get(o.getClientId());
                if (c != null && c.getName() != null) clientText = c.getName();
            }
            ((TextView) h.itemView.findViewById(R.id.tvOrderClient)).setText(clientText);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            ((TextView) h.itemView.findViewById(R.id.tvOrderDate)).setText(df.format(new Date(o.getDateEpochMillis())));
            ((TextView) h.itemView.findViewById(R.id.tvOrderItems)).setText(getString(R.string.order_items_count_format, o.itemCount()));
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            ((TextView) h.itemView.findViewById(R.id.tvOrderTotalPrice)).setText(nf.format(calcTotal(o)));
            h.itemView.setOnClickListener(v -> openOrderDetailDialog(o));
        }
        @Override public int getItemCount() { return visible.size(); }
        class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
    }

    private int mapStatusBg(String code) {
        switch (code) {
            case STATUS_DELIVERED: return R.drawable.bg_badge_delivered;
            case STATUS_CANCELLED: return R.drawable.bg_badge_cancelled;
            case STATUS_DRAFT: return R.drawable.bg_badge_draft;
            default: return R.drawable.bg_badge_pending;
        }
    }

    private void openOrderDetailDialog(Order orderParam) {
        Order fresh = orderRepo.get(orderParam.getId() != null ? orderParam.getId() : -1);
        final Order finalOrder = fresh != null ? fresh : orderParam;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail, null, false);
        ((TextView) view.findViewById(R.id.tvDetailOrderTitle)).setText(getString(R.string.order_detail_title_format, finalOrder.getNumber()));
        ((TextView) view.findViewById(R.id.tvDetailClient)).setText(finalOrder.getClientId() != null ? (clientRepo.get(finalOrder.getClientId()) != null ? clientRepo.get(finalOrder.getClientId()).getName() : "—") : "—");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        ((TextView) view.findViewById(R.id.tvDetailDate)).setText(df.format(new Date(finalOrder.getDateEpochMillis())));
        ((TextView) view.findViewById(R.id.tvDetailStatus)).setText(getStatusLabelByCode(finalOrder.getStatus()));
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        ((TextView) view.findViewById(R.id.tvDetailTotal)).setText(nf.format(calcTotal(finalOrder))); // use fallback total
        LinearLayout itemsContainer = view.findViewById(R.id.containerDetailItems);
        if (finalOrder.getItems() != null) {
            for (OrderItem it : finalOrder.getItems()) {
                TextView tv = new TextView(this);
                String wineName = it.getWine() != null ? it.getWine().getName() : (it.getWineId() != null ? buscaWineNome(it.getWineId()) : getString(R.string.generic_item));
                tv.setText(getString(R.string.order_item_line, wineName, it.getQuantity()));
                tv.setTextColor(0xFF444444);
                tv.setTextSize(14f);
                itemsContainer.addView(tv);
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        view.findViewById(R.id.btnEditOrder).setOnClickListener(v -> {
            dialog.dismiss();
            openEditOrderDialog(finalOrder);
        });
        view.findViewById(R.id.btnDeleteOrder).setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_order_title)
                    .setMessage(R.string.delete_order_confirm)
                    .setPositiveButton(R.string.dialog_delete, (d,w) -> {
                        if (finalOrder.getId() != null) orderRepo.delete(finalOrder.getId());
                        loadOrdersFromDb();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        });
        dialog.show();
    }

    private String buscaWineNome(Long wineId) {
        if (wineId == null) return getString(R.string.generic_item);
        for (Wine w : wineDao.findAll()) {
            if (w.getId() != null && w.getId().equals(wineId)) return w.getName();
        }
        return getString(R.string.generic_item);
    }

    private void openEditOrderDialog(Order orderParam) {
        Order fresh = orderRepo.get(orderParam.getId() != null ? orderParam.getId() : -1);
        final Order finalOrder = fresh != null ? fresh : orderParam;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_order, null, false);
        Spinner spClient = dialogView.findViewById(R.id.spClient);
        Spinner spPayment = dialogView.findViewById(R.id.spPayment);
        Spinner spStatus = dialogView.findViewById(R.id.spStatus);
        Button btnAddProductRow = dialogView.findViewById(R.id.btnAddProductRow);
        ViewGroup containerProducts = dialogView.findViewById(R.id.containerProducts);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOrder);
        Button btnCreate = dialogView.findViewById(R.id.btnCreateOrder);
        btnCreate.setText(R.string.action_edit);

        List<Client> clients = clientRepo.all();
        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mapClients(clients));
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClient.setAdapter(clientAdapter);
        for (int i = 0; i < clients.size(); i++) {
            if (finalOrder.getClientId() != null && finalOrder.getClientId().equals(clients.get(i).getId())) { spClient.setSelection(i); break; }
        }
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Pagamento Pendente", "Pagamento Feito"});
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(paymentAdapter);
        for (int i = 0; i < paymentAdapter.getCount(); i++) {
            String item = paymentAdapter.getItem(i);
            String currentPaymentLabel = "Pagamento Pendente";
            if ("PAID".equals(finalOrder.getPayment())) currentPaymentLabel = "Pagamento Feito";
            if (item != null && item.equals(currentPaymentLabel)) { spPayment.setSelection(i); break; }
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getStatusLabels());
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);
        String currentLabel = getStatusLabelByCode(finalOrder.getStatus());
        for (int i = 0; i < statusAdapter.getCount(); i++) {
            String lbl = statusAdapter.getItem(i);
            if (lbl != null && lbl.equals(currentLabel)) { spStatus.setSelection(i); break; }
        }

        // Editable items: repeater preenchido com os itens do pedido
        List<Wine> allWines = wineDao.findAll();
        containerProducts.removeAllViews();
        if (finalOrder.getItems() != null && !finalOrder.getItems().isEmpty()) {
            for (OrderItem it : finalOrder.getItems()) {
                addProductRow(containerProducts, it.getWineId());
                View row = containerProducts.getChildAt(containerProducts.getChildCount()-1);
                if (row instanceof LinearLayout) {
                    for (int j=0;j<((LinearLayout)row).getChildCount();j++) {
                        View inner = ((LinearLayout)row).getChildAt(j);
                        if (inner instanceof EditText) {
                          ((EditText)inner).setText(String.valueOf(it.getQuantity() != null ? it.getQuantity() : 1));
                        }
                    }
                }
            }
        } else {
            addProductRow(containerProducts, null);
        }
        btnAddProductRow.setVisibility(View.VISIBLE);
        btnAddProductRow.setOnClickListener(v -> addProductRow(containerProducts, null));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            int clientIndex = spClient.getSelectedItemPosition();
            if (clientIndex >= 0 && clientIndex < clients.size()) finalOrder.setClientId(clients.get(clientIndex).getId());
            String previousStatus = finalOrder.getStatus();
            String paymentStateLabel = (String) spPayment.getSelectedItem();
            finalOrder.setPayment("Pagamento Feito".equals(paymentStateLabel) ? "PAID" : "PENDING");
            finalOrder.setStatus(getStatusCodeByLabel((String) spStatus.getSelectedItem()));
            List<OrderItem> newItems = collectItemsPersistent(containerProducts);
            if (newItems.isEmpty()) {
                Toast.makeText(this, R.string.error_order_need_item, Toast.LENGTH_SHORT).show();
                return;
            }
            finalOrder.setItems(newItems);
            orderRepo.update(finalOrder); // persiste alterações
            loadOrdersFromDb(); // recarrega lista
            dialog.dismiss();
        });
        dialog.show();
    }
}
