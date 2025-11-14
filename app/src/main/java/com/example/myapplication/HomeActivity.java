package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        RecyclerView rv = findViewById(R.id.rvBottomCarousel);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(lm);
        rv.setItemViewCacheSize(5);

        List<BottomItem> items = new ArrayList<>();
        items.add(new BottomItem("Catálogo", R.drawable.ic_cup_straw));
        items.add(new BottomItem("Clientes", R.drawable.ic_person));
        items.add(new BottomItem("Pedidos", R.drawable.ic_cart));
        items.add(new BottomItem("Rotas", R.drawable.ic_map));

        BottomCarouselAdapter adapter = new BottomCarouselAdapter(items, (pos, item) -> {
            // TODO: navegação
        });
        rv.setAdapter(adapter);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rv);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
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
        rv.scrollToPosition(0);
    }
}
