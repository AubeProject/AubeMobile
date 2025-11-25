package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Set;

import com.example.myapplication.data.model.Order;
import com.example.myapplication.data.model.OrderItem;
import com.example.myapplication.data.repository.OrderRepository;

public class HomeActivity extends AppCompatActivity {

    private enum Period { WEEK, MONTH, YEAR }
    private Period currentPeriod = Period.MONTH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupPeriodSelector();
        setupBottomNav();
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData(); // refresh when returning from other screens
    }

    private void setupPeriodSelector() {
        TextView tvSemana = findViewById(R.id.tvSemana);
        TextView tvMes = findViewById(R.id.tvMes);
        TextView tvAno = findViewById(R.id.tvAno);
        View.OnClickListener l = v -> {
            if (v.getId() == R.id.tvSemana) currentPeriod = Period.WEEK;
            else if (v.getId() == R.id.tvMes) currentPeriod = Period.MONTH;
            else if (v.getId() == R.id.tvAno) currentPeriod = Period.YEAR;
            highlightPeriodSelection(tvSemana, tvMes, tvAno, (TextView) v);
            loadDashboardData();
        };
        tvSemana.setOnClickListener(l);
        tvMes.setOnClickListener(l);
        tvAno.setOnClickListener(l);
        highlightPeriodSelection(tvSemana, tvMes, tvAno, tvMes); // default MONTH selected
    }

    private void highlightPeriodSelection(TextView semana, TextView mes, TextView ano, TextView selected) {
        semana.setBackground(null); semana.setTextColor(0xFFBDBDBD);
        mes.setBackground(null); mes.setTextColor(0xFFBDBDBD);
        ano.setBackground(null); ano.setTextColor(0xFFBDBDBD);
        selected.setBackgroundColor(0xFF1F1F1F); selected.setTextColor(0xFFFFFFFF);
    }

    private void loadDashboardData() {
        OrderRepository repo = new OrderRepository(this);
        long now = System.currentTimeMillis();
        long start;
        switch (currentPeriod) {
            case WEEK: start = now - 7L*24*60*60*1000; break;
            case MONTH: start = now - 30L*24*60*60*1000; break;
            case YEAR: default: start = now - 365L*24*60*60*1000; break;
        }
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));
        List<Order> orders = repo.all();
        double total = 0.0; int entregues = 0; int pendentes = 0; Set<Long> clientesSet = new HashSet<>();
        for (Order o : orders) {
            if (o == null || o.getDateEpochMillis() == null) continue;
            long d = o.getDateEpochMillis(); if (d < start || d > now) continue;
            if ("DELIVERED".equalsIgnoreCase(o.getStatus())) entregues++; else if ("PENDING".equalsIgnoreCase(o.getStatus())) pendentes++;
            if (o.getClientId() != null) clientesSet.add(o.getClientId());
            boolean paid = "PAID".equalsIgnoreCase(o.getPayment());
            if (paid && o.getItems() != null) for (OrderItem it : o.getItems()) if (it != null && it.getWine() != null && it.getWine().getPrice() != null && it.getQuantity() != null) total += it.getWine().getPrice() * it.getQuantity();
        }
        int clientesVisitados = clientesSet.size();
        TextView tvTotalValue = findViewById(R.id.tvTotalValue);
        TextView tvTotalDelta = findViewById(R.id.tvTotalDelta);
        TextView tvClientesValue = findViewById(R.id.tvClientesValue);
        TextView tvClientesDelta = findViewById(R.id.tvClientesDelta);
        TextView tvPedidosValue = findViewById(R.id.tvPedidosValue);
        TextView tvPedidosPendentes = findViewById(R.id.tvPedidosPendentes);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        if (tvTotalValue != null) tvTotalValue.setText(currency.format(total));
        if (tvTotalDelta != null) tvTotalDelta.setText(getString(R.string.dashboard_pending_format, pendentes));
        if (tvClientesValue != null) tvClientesValue.setText(String.valueOf(clientesVisitados));
        if (tvClientesDelta != null) tvClientesDelta.setText(getString(R.string.dashboard_orders_format, entregues));
        if (tvPedidosValue != null) tvPedidosValue.setText(String.valueOf(entregues));
        if (tvPedidosPendentes != null) tvPedidosPendentes.setText(getString(R.string.dashboard_pending_format, pendentes));
        if (tvSubtitle != null) tvSubtitle.setText(getString(R.string.dashboard_period_summary_format, clientesVisitados, entregues));
    }

    private void setupBottomNav() {
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
        BottomCarouselAdapter adapter = new BottomCarouselAdapter(items, (pos, item) -> {
            String title = item.getTitle();
            if ("Dashboard".equals(title)) return; // current
            Intent intent = null;
            if ("Catálogo".equals(title)) intent = new Intent(this, CatalogActivity.class);
            else if ("Clientes".equals(title)) intent = new Intent(this, ClientsActivity.class);
            else if ("Pedidos".equals(title)) intent = new Intent(this, OrdersActivity.class);
            else if ("Rotas".equals(title)) intent = new Intent(this, RoutesActivity.class);
            if (intent != null) startActivity(intent);
        });
        bottomNav.setAdapter(adapter);
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(bottomNav);
        bottomNav.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(lm);
                    if (centerView != null) {
                        int pos = lm.getPosition(centerView);
                        adapter.setSelectedPosition(pos);
                    }
                }
            }
        });
        adapter.setSelectedPosition(0);
        bottomNav.scrollToPosition(0);
    }
}
