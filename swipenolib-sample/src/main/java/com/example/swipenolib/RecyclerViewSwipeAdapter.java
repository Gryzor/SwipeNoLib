package com.example.swipenolib;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

class RecyclerViewSwipeAdapter extends RecyclerView.Adapter<RecyclerViewSwipeViewHolder> {

    private List<String> items = new ArrayList<>();

    RecyclerViewSwipeAdapter(final List<String> items) {
        this.items = items;
    }

    @Override
    public RecyclerViewSwipeViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View view = inflater.inflate(R.layout.item_swipe, parent, false);
        return new RecyclerViewSwipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewSwipeViewHolder holder, final int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Removes an item from the adapter, if it exists.
     * @param position the desired position of the item to remove.
     */
    void remove(int position) {
        final String item = items.get(position);

        if (items.contains(item)) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }
}
