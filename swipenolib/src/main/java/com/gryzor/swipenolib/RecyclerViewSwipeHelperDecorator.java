package com.gryzor.swipenolib;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * A custom {@link android.support.v7.widget.RecyclerView.ItemDecoration} that plays alongside the implementation
 * of {@link RecyclerViewItemSwipeHelper} to support swipe actions.
 */
public class RecyclerViewSwipeHelperDecorator extends RecyclerView.ItemDecoration {

    private Drawable background;
    private boolean initiated;

    private RecyclerViewSwipeHelperDecorator(final Builder builder) {
        background = builder.background;
    }

    private void init() {
        if (background == null) {
            // Set a Default
            Log.w("SWIPE", "Warning: you didn't pass a background color, using RED.");
            background = new ColorDrawable(Color.RED);
        }
        initiated = true;
    }

    @Override
    public void onDraw(final Canvas canvas, final RecyclerView parent, final RecyclerView.State state) {
        if (!initiated) {
            init();
        }

        // If an animation is in progress we need to draw the background.
        if (parent.getItemAnimator().isRunning()) {
            // When an item is removed, other items may need to move up and others down (at the same time).
            // For example, when you remove something in the middle of the list.

            // Step 1:
            // Find first child with translationY > 0 and last one with translationY < 0
            // we're after a rect that is not covered in recycler-view views at this point in time
            View lastViewComingDown = null;
            View firstViewComingUp = null;

            // One side is always fixed
            int left = 0;
            int right = parent.getWidth();

            // The other we need to find out
            int top = 0;
            int bottom = 0;

            // Find translating views
            int childCount = parent.getLayoutManager().getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getLayoutManager().getChildAt(i);
                if (child.getTranslationY() < 0) {
                    // View is coming down
                    lastViewComingDown = child;
                } else if (child.getTranslationY() > 0) {
                    // view is coming up
                    if (firstViewComingUp == null) {
                        firstViewComingUp = child;
                    }
                }
            }

            if (lastViewComingDown != null && firstViewComingUp != null) {
                // Views are coming down AND going up to fill the void
                top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            } else if (lastViewComingDown != null) {
                // Views are going down to fill the void.
                top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                bottom = lastViewComingDown.getBottom();
            } else if (firstViewComingUp != null) {
                // views are coming up to fill the void
                top = firstViewComingUp.getTop();
                bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            }

            // Finally Draw the background.
            background.setBounds(left, top, right, bottom);
            background.draw(canvas);
        }

        super.onDraw(canvas, parent, state);
    }

    /**
     * Use this Builder to construct your RecyclerView.ItemDecoration.
     */
    public static class Builder {
        private Drawable background;

        public Builder setBackgroundColor(Drawable drawable) {
            this.background = drawable;
            return this;
        }

        public RecyclerView.ItemDecoration build() {
            return new RecyclerViewSwipeHelperDecorator(this);
        }

        /**
         * Convenience method to build and add a {@link RecyclerViewSwipeHelperDecorator} to a RecyclerView.
         * @param recyclerView A valid <b>non-null</b> instance of a RecyclerView to attach a decorator to.
         * @return the built instance of the {@link android.support.v7.widget.RecyclerView.ItemDecoration}, already
         * added to the supplied RecyclerView.
         */
        public RecyclerView.ItemDecoration buildAndAdd(@NonNull final RecyclerView recyclerView) {
            final RecyclerViewSwipeHelperDecorator decorator = new RecyclerViewSwipeHelperDecorator(this);
            recyclerView.addItemDecoration(decorator);
            return decorator;
        }
    }
}
