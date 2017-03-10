package com.example.swipenolib;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class RecyclerViewSwipeViewHolder extends RecyclerView.ViewHolder {

    private final TextView textView;

    RecyclerViewSwipeViewHolder(final View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.item_text);
    }

    void bind(String value) {
        textView.setText(value);
    }
}
