package com.example.swipenolib;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.gryzor.swipenolib.RecyclerViewSwipeHelperDecorator;
import com.gryzor.swipenolib.RecyclerViewItemSwipeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final List<String> strings = new ArrayList<>(
                Arrays.asList(
                        "One (Swipe Disabled)", "Two", "Three", "Four (Swipe Disabled)", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
                        "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen",
                        "Twenty (Swipe Disabled)"
                )
        );

        final RecyclerViewSwipeAdapter adapter = new RecyclerViewSwipeAdapter(strings);
        // Add Swipe To Delete Support.
        new RecyclerViewItemSwipeHelper.Builder()
                .setBackgroundColor(new ColorDrawable(Color.RED))
                .setSwipeListener(new RecyclerViewItemSwipeHelper.OnSwipeListener() {
                    @Override
                    public void onItemSwiped(final int position) {
                        adapter.remove(position);
                        Toast.makeText(SwipeActivity.this, "Removing position: " + position, Toast.LENGTH_SHORT).show();
                    }
                })
                .swipeToStart()
                .setDeleteImageColor(Color.WHITE)
                .disableSwipeOnPositions(0, 3)
                .disableSwipeOnLastItem()
                .buildAndAttach(this, recyclerView);

        // Needed to display the "background color" during the animation after a delete.
        new RecyclerViewSwipeHelperDecorator.Builder()
                .setBackgroundColor(new ColorDrawable(Color.RED))
                .buildAndAdd(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
