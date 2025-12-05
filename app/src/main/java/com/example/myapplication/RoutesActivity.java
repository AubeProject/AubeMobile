package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.dao.WineDao;
import com.example.myapplication.data.dao.impl.WineDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Client;
import com.example.myapplication.data.model.Company;
import com.example.myapplication.data.model.Order;
import com.example.myapplication.data.model.OrderItem;
import com.example.myapplication.data.model.Wine;
import com.example.myapplication.data.repository.ClientRepository;
import com.example.myapplication.data.repository.CompanyRepository;
import com.example.myapplication.data.repository.OrderRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private OrderRepository orderRepo;
    private ClientRepository clientRepo;
    private CompanyRepository companyRepo;
    private WineDao wineDao;
    private RecyclerView rvPendingOrders;
    private PendingOrdersAdapter ordersAdapter;
    private LinearLayoutManager listLayoutManager;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = null;
                Boolean coarseLocationGranted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                } else {
                    fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                }

                if ((fineLocationGranted != null && fineLocationGranted) || 
                    (coarseLocationGranted != null && coarseLocationGranted)) {
                    enableMyLocation();
                } else {
                    Toast.makeText(this, "Permissão de localização necessária para mostrar sua posição", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        setTitle("Rotas");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        orderRepo = new OrderRepository(this);
        clientRepo = new ClientRepository(this);
        companyRepo = new CompanyRepository(this);
        wineDao = new WineDaoImpl(AppDatabase.getInstance(this));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        rvPendingOrders = findViewById(R.id.rvPendingOrders);
        listLayoutManager = new LinearLayoutManager(this);
        rvPendingOrders.setLayoutManager(listLayoutManager);
        ordersAdapter = new PendingOrdersAdapter();
        rvPendingOrders.setAdapter(ordersAdapter);

        findViewById(R.id.btnDefineCompany).setOnClickListener(v -> defineCompanyLocation());
        findViewById(R.id.iniciarRota).setOnClickListener(v -> initiateRouteProcess());

        loadPendingOrdersList();
        setupBottomCarousel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingOrdersList();
        if (mMap != null) {
            mMap.clear();
            loadPendingOrdersOnMap();
            loadCompanyMarker();
        }
    }

    private void loadPendingOrdersList() {
        List<Order> all = orderRepo.all();
        List<Order> pending = new ArrayList<>();
        for (Order o : all) {
            if ("PENDING".equals(o.getStatus()) || "Pendente".equals(o.getStatus())) {
                pending.add(o);
            }
        }
        runOnUiThread(() -> ordersAdapter.setData(pending));
    }

    private void setupBottomCarousel() {
        RecyclerView recycler = findViewById(R.id.bottomNavOrders);
        if (recycler == null) return;

        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<BottomItem> items = new ArrayList<>();
        items.add(new BottomItem("Dashboard", R.drawable.ic_dashboard));
        items.add(new BottomItem("Catálogo", R.drawable.ic_cup_straw));
        items.add(new BottomItem("Clientes", R.drawable.ic_person));
        items.add(new BottomItem("Pedidos", R.drawable.ic_cart));
        items.add(new BottomItem("Rotas", R.drawable.ic_map));

        BottomCarouselAdapter adapter = new BottomCarouselAdapter(items, (pos, item) -> {
            String title = item.getTitle();
            if ("Rotas".equals(title)) return;
            
            Intent intent = null;
            if ("Dashboard".equals(title)) intent = new Intent(this, HomeActivity.class);
            else if ("Catálogo".equals(title)) intent = new Intent(this, CatalogActivity.class);
            else if ("Clientes".equals(title)) intent = new Intent(this, ClientsActivity.class);
            else if ("Pedidos".equals(title)) intent = new Intent(this, OrdersActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0); 
            }
        });

        recycler.setAdapter(adapter);
        adapter.setSelectedPosition(4);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        loadPendingOrdersOnMap();
        
        mMap.setOnMapLongClickListener(latLng -> {
            showLinkAddressDialog(latLng);
        });

        loadCompanyMarker();
    }

    private void initiateRouteProcess() {
        List<Order> pendingOrders = ordersAdapter.getData();
        if (pendingOrders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido pendente para rota.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> customerOptions = new ArrayList<>();
        List<Order> selectionMapping = new ArrayList<>();
        boolean[] checkedItems = new boolean[pendingOrders.size()];

        for (int i = 0; i < pendingOrders.size(); i++) {
            Order o = pendingOrders.get(i);
            String clientName = "Cliente Desconhecido";
            if (o.getClientId() != null) {
                Client c = clientRepo.get(o.getClientId());
                if (c != null) clientName = c.getName();
            }
            customerOptions.add("Pedido #" + o.getNumber() + " - " + clientName);
            selectionMapping.add(o);
            checkedItems[i] = true; // Default select all
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecione os pedidos para a rota")
                .setMultiChoiceItems(customerOptions.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Continuar", (dialog, which) -> {
                    List<Order> selectedOrders = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedOrders.add(selectionMapping.get(i));
                        }
                    }
                    showReorderDialog(selectedOrders);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showReorderDialog(List<Order> selectedOrders) {
        if (selectedOrders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido selecionado.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reorder_route, null);
        RecyclerView rvReorder = dialogView.findViewById(R.id.rvReorderRoute);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmRoute);

        RouteReorderAdapter reorderAdapter = new RouteReorderAdapter(selectedOrders, clientRepo);
        rvReorder.setLayoutManager(new LinearLayoutManager(this));
        rvReorder.setAdapter(reorderAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                Collections.swap(selectedOrders, fromPos, toPos);
                reorderAdapter.notifyItemMoved(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rvReorder);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnConfirm.setOnClickListener(v -> {
            launchGoogleMapsRoute(selectedOrders);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void launchGoogleMapsRoute(List<Order> selectedOrders) {
        if (selectedOrders.isEmpty()) {
            Toast.makeText(this, "Nenhum pedido selecionado.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<String> validAddresses = new ArrayList<>();
            
            for (Order order : selectedOrders) {
                 if (order.getClientId() != null) {
                    Client c = clientRepo.get(order.getClientId());
                    if (c != null && c.getAddress() != null && !c.getAddress().isEmpty()) {
                        validAddresses.add(Uri.encode(c.getAddress()));
                    }
                 }
            }
            
            if (validAddresses.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Nenhum endereço válido encontrado nos pedidos selecionados.", Toast.LENGTH_LONG).show());
                return;
            }
            
            String destination = validAddresses.get(validAddresses.size() - 1);
            String waypointsStr = "";
            
            if (validAddresses.size() > 1) {
                StringBuilder wpBuilder = new StringBuilder();
                for (int i = 0; i < validAddresses.size() - 1; i++) {
                    if (wpBuilder.length() > 0) wpBuilder.append("|");
                    wpBuilder.append(validAddresses.get(i));
                }
                waypointsStr = wpBuilder.toString();
            }

            String url = "https://www.google.com/maps/dir/?api=1&origin=My+Location&destination=" + destination + "&travelmode=driving";
            if (!waypointsStr.isEmpty()) {
                url += "&waypoints=" + waypointsStr;
            }

            String finalUrl = url;
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    intent.setPackage(null);
                    startActivity(intent);
                }
            });

        }).start();
    }

    private LatLng getLatLngFromAddress(String address, Geocoder geocoder) {
        if (address == null) return null;
        if (address.matches("-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?")) {
            String[] parts = address.split(",");
            try {
                return new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            } catch (Exception e) { return null; }
        }
        try {
            List<Address> list = geocoder.getFromLocationName(address, 1);
            if (list != null && !list.isEmpty()) {
                return new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude());
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    private void loadCompanyMarker() {
        new Thread(() -> {
            Company comp = companyRepo.getFirstCompany();
            if (comp != null && comp.getLatitude() != null && comp.getLongitude() != null) {
                LatLng pos = new LatLng(comp.getLatitude(), comp.getLongitude());
                runOnUiThread(() -> {
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title("Empresa: " + comp.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }
                });
            }
        }).start();
    }

    private void defineCompanyLocation() {
        if (mMap == null) return;
        LatLng center = mMap.getCameraPosition().target;
        
        new AlertDialog.Builder(this)
                .setTitle("Definir Local da Empresa")
                .setMessage("Deseja definir o centro atual do mapa como local da empresa?")
                .setPositiveButton("Sim", (d, w) -> {
                    saveCompanyLocation(center);
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void saveCompanyLocation(LatLng latLng) {
        new Thread(() -> {
            Company existing = companyRepo.getFirstCompany();
            if (existing == null) {
                existing = new Company("Minha Empresa", "Local no Mapa");
                existing.setLatitude(latLng.latitude);
                existing.setLongitude(latLng.longitude);
                
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        existing.setAddress(addresses.get(0).getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                companyRepo.insert(existing);
            } else {
                existing.setLatitude(latLng.latitude);
                existing.setLongitude(latLng.longitude);
                
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        existing.setAddress(addresses.get(0).getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                companyRepo.update(existing);
            }
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Local da empresa salvo!", Toast.LENGTH_SHORT).show();
                if (mMap != null) {
                    mMap.clear();
                    loadPendingOrdersOnMap();
                    loadCompanyMarker();
                }
            });
        }).start();
    }

    private void showLinkAddressDialog(LatLng latLng) {
        List<Client> clients = clientRepo.all();
        String[] clientNames = new String[clients.size()];
        for(int i=0; i<clients.size(); i++) clientNames[i] = clients.get(i).getName();

        new AlertDialog.Builder(this)
                .setTitle("Vincular Local a Cliente")
                .setItems(clientNames, (dialog, which) -> {
                    Client selected = clients.get(which);
                    updateClientAddress(selected, latLng);
                })
                .show();
    }

    private void updateClientAddress(Client client, LatLng latLng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String addressStr = client.getAddress(); // fallback
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    addressStr = addresses.get(0).getAddressLine(0);
                } else {
                    addressStr = latLng.latitude + "," + latLng.longitude;
                }
            } catch (IOException e) {
                Log.e("RoutesActivity", "Geocoding falhou", e);
                addressStr = latLng.latitude + "," + latLng.longitude;
            }

            client.setAddress(addressStr);
            clientRepo.update(client); 

            runOnUiThread(() -> {
                 Toast.makeText(this, "Endereço atualizado para " + client.getName(), Toast.LENGTH_SHORT).show();
                 if (mMap != null) {
                    mMap.clear();
                    loadPendingOrdersOnMap();
                    loadCompanyMarker();
                 }
            });
        }).start();
    }

    private void loadPendingOrdersOnMap() {
        if (mMap == null) return;

        new Thread(() -> {
            List<Order> orders = orderRepo.all();
            Geocoder geocoder = new Geocoder(RoutesActivity.this, Locale.getDefault());
            boolean foundAny = false;

            for (Order order : orders) {
                if (("PENDING".equals(order.getStatus()) || "Pendente".equals(order.getStatus())) 
                        && order.getClientId() != null) {
                    Client client = clientRepo.get(order.getClientId());
                    if (client != null && client.getAddress() != null && !client.getAddress().isEmpty()) {
                        LatLng latLng = getLatLngFromAddress(client.getAddress(), geocoder);
                        if (latLng != null) {
                            final LatLng finalLatLng = latLng;
                            runOnUiThread(() -> {
                                mMap.addMarker(new MarkerOptions()
                                        .position(finalLatLng)
                                        .title("Pedido #" + order.getNumber())
                                        .snippet(client.getName())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                            });
                            foundAny = true;
                        }
                    }
                }
            }
        }).start();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12)); 
                    }
                });
    }

    private double calcTotal(Order o) {
        double sum = 0.0;
        if (o == null || o.getItems() == null) return 0.0;
        for (OrderItem it : o.getItems()) {
            if (it == null) continue;
            Wine w = it.getWine();
            if ((w == null || w.getPrice() == null) && it.getWineId() != null) {
                try { w = wineDao.findById(it.getWineId()); } catch (Exception ignored) {}
            }
            Double price = (w != null ? w.getPrice() : null);
            Integer qty = it.getQuantity();
            if (price != null && qty != null) sum += price * qty;
        }
        return sum;
    }

    class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.VH> {
        private final List<Order> data = new ArrayList<>();
        public List<Order> getData() { return data; }

        void setData(List<Order> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false); 
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Order o = data.get(position);
            
            ((TextView) h.itemView.findViewById(R.id.tvOrderTitle)).setText(getString(R.string.order_title_card_format, o.getNumber()));
            TextView statusView = h.itemView.findViewById(R.id.tvOrderStatus);
            statusView.setText(getString(R.string.status_pending));
            statusView.setBackgroundResource(R.drawable.bg_badge_pending);
            
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
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            VH(View itemView) { super(itemView); }
        }
    }
    
    class RouteReorderAdapter extends RecyclerView.Adapter<RouteReorderAdapter.VH> {
        private List<Order> items;
        private ClientRepository repo;
        public RouteReorderAdapter(List<Order> items, ClientRepository repo) { 
            this.items = items; 
            this.repo = repo;
        }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_reorder, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Order o = items.get(position);
            h.title.setText("Pedido #" + o.getNumber());
            if (o.getClientId() != null) {
                Client c = repo.get(o.getClientId());
                h.subtitle.setText(c != null ? c.getName() : "Cliente Desconhecido");
            } else h.subtitle.setText("-");
        }
        @Override public int getItemCount() { return items.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            VH(View v) { 
                super(v); 
                title = v.findViewById(R.id.tvReorderTitle);
                subtitle = v.findViewById(R.id.tvReorderSubtitle);
            }
        }
    }
}