package com.gryzor.swipenolib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.util.Arrays;

/**
 * A {@link ItemTouchHelper} implementation to notify a {@link OnSwipeListener} that a particular position in
 * a RecyclerView adapter has been swiped.
 * Use {@link Builder} to construct an instance.
 * This implementation ignores Drag and Drop and only does Swipe (end/start/both).
 * The idea for this implementation came from
 * @see <a href="https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete">https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete</a>
 */
public final class RecyclerViewItemSwipeHelper extends ItemTouchHelper.SimpleCallback {

    private static final String TAG = RecyclerViewItemSwipeHelper.class.toString();
    private final int[] disabledLocations;
    private Context context;
    private OnSwipeListener listener;
    private Drawable background;
    private Drawable deleteIcon;
    private int deleteIconColor = Color.WHITE;
    private int deleteIconMargin;
    private boolean initiated;
    private boolean disableSwipeOnLastPosition;

    private RecyclerViewItemSwipeHelper(final Context context, final Builder builder) {
        super(0, builder.swipeDirs);
        this.context = context;
        this.background = builder.background;
        this.deleteIcon = builder.deleteIcon;
        this.deleteIconMargin = builder.deleteIconMargin;
        this.listener = builder.listener;
        this.deleteIconColor = builder.deleteIconColor;
        this.disabledLocations = builder.disabledLocations;
        this.disableSwipeOnLastPosition = builder.disableSwipeOnLastPosition;
    }

    private void init() {
        // Ensure we have some defaults.
        if (context == null) {
            throw new NullPointerException("You need a valid non-null Context to use this class");
        }

        if (background == null) {
            // Set a Default
            Log.i(TAG, "Warning: you didn't pass a background color, using RED.");
            background = new ColorDrawable(Color.RED);
        }

        if (deleteIcon == null) {
            Log.i(TAG, "Warning: you didn't pass a delete icon, using R.drawable.vg_clear_black_24dp.");
            deleteIcon = ContextCompat.getDrawable(context, R.drawable.vg_clear_black_24dp);
        }

        deleteIcon.setColorFilter(deleteIconColor, PorterDuff.Mode.SRC_ATOP);

        if (deleteIconMargin < 0) {
            deleteIconMargin = (int) context.getResources().getDimension(R.dimen.swipe_cell_delete_image_margin);
        }

        if (listener == null) {
            Log.i(TAG, "Warning: you have no listener, you will not get notifications when a row is swiped.");
        }

        if (disabledLocations != null) {
            Arrays.sort(disabledLocations);
        }

        initiated = true;
    }

    @Override
    public boolean onMove(
            final RecyclerView recyclerView,
            final RecyclerView.ViewHolder viewHolder,
            final RecyclerView.ViewHolder target) {
        // This would be useful for Drag and Drop, which we don't need/support.
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
        int swipedPosition = viewHolder.getAdapterPosition();
        if (listener != null) {
            listener.onItemSwiped(swipedPosition);
        }
    }

    @Override
    public int getSwipeDirs(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        int currPos = viewHolder.getAdapterPosition();
        int lastPos = recyclerView.getAdapter().getItemCount() - 1;

        if (Arrays.binarySearch(disabledLocations, currPos) >= 0
                || (currPos == lastPos && disableSwipeOnLastPosition)) {
            return 0;
        }

        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onChildDraw(
            final Canvas canvas,
            final RecyclerView recyclerView,
            final RecyclerView.ViewHolder viewHolder,
            final float deltaX,
            final float deltaY,
            final int actionState,
            final boolean isCurrentlyActive) {

        final View itemView = viewHolder.itemView;

        // This method may get called for ViewHolders that are already swiped away, discard those calls.
        if (viewHolder.getAdapterPosition() == RecyclerView.NO_POSITION) {
            return;
        }

        if (!initiated) {
            init();
        }

        // Draw Background
        background.setBounds(
                itemView.getRight() + (int) deltaX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom());

        background.draw(canvas);

        // Draw Icon (X)
        int itemHeight = itemView.getBottom() - itemView.getTop();
        int intrinsicWidth = deleteIcon.getIntrinsicWidth();
        int intrinsicHeight = deleteIcon.getIntrinsicWidth();

        int iconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
        int iconRight = itemView.getRight() - deleteIconMargin;
        int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int iconBottom = iconTop + intrinsicHeight;
        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        deleteIcon.draw(canvas);

        super.onChildDraw(canvas, recyclerView, viewHolder, deltaX, deltaY, actionState, isCurrentlyActive);
    }

    /**
     * Interface to receive Swipe Notifications.
     */
    public interface OnSwipeListener {
        void onItemSwiped(int position);
    }

    /**
     * Use this builder to construct instances of {@link ItemTouchHelper}.
     */
    public static class Builder {
        private int swipeDirs;
        private Drawable background;
        private Drawable deleteIcon;
        private int deleteIconColor;
        private int deleteIconMargin = -1;
        private OnSwipeListener listener;
        private int[] disabledLocations;
        private boolean disableSwipeOnLastPosition;

        /**
         * Enable Swipe from Start (Left in Left-To-Right languages).
         */
        public Builder swipeToStart() {
            swipeDirs |= ItemTouchHelper.START;
            return this;
        }

        /**
         * Enable Swipe from End (Right in Left-To-Right languages).
         */
        public Builder swipeToEnd() {
            swipeDirs |= ItemTouchHelper.END;
            return this;
        }

        /**
         * The Drawable used behind the Swipe.
         * @param drawable a valid, non-null drawable.
         */
        public Builder setBackgroundColor(Drawable drawable) {
            this.background = drawable;
            return this;
        }

        /**
         * The Drawable that will be shown behind the swipe (on the end/right side only).
         * @param drawable a valid, non-null drawable.
         */
        public Builder setDeleteImage(Drawable drawable) {
            this.deleteIcon = drawable;
            return this;
        }

        /**
         * Adds a Margin to the delete image, defaults to 16dp.
         * @param dp a valid margin in dp.
         */
        public Builder setDeleteImageMargin(int dp) {
            this.deleteIconMargin = dp;
            return this;
        }

        /**
         * The drawable displayed as delete image will be painted with this color via {@code setColorFilter()}.
         * @param color a valid color.
         */
        public Builder setDeleteImageColor(int color) {
            this.deleteIconColor = color;
            return this;
        }

        /**
         * If you want to disable certain positions (like the first one), pass them here.
         * This is mostly useful to disable position 0 (initial) and/or any other hardcoded headers.
         * @param positions The positions you want to ignore swipes.
         */
        public Builder disableSwipeOnPositions(int... positions) {
            disabledLocations = positions;
            return this;
        }

        /**
         * Since the last item is usually dynamic, you can disable swipe on the last item and the touch helper will
         * keep track of it.
         */
        public Builder disableSwipeOnLastItem() {
            disableSwipeOnLastPosition = true;
            return this;
        }

        /**
         * If you want to be notified when a Swipe happens, pass a listener here.
         * @param listener a valid listener. If null, no listener will be notified.
         */
        public Builder setSwipeListener(OnSwipeListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Build an instance of {@link ItemTouchHelper} that can be attached to a recycler view.
         * @param context a valid <b>non-null</b> Context (used to inflate drawables and such).
         * @return a valid instance of {@link ItemTouchHelper}.
         */
        public ItemTouchHelper build(@NonNull Context context) {
            RecyclerViewItemSwipeHelper callback = new RecyclerViewItemSwipeHelper(context, this);
            return new ItemTouchHelper(callback);
        }

        /**
         * Convenience method to Build and attach an instance of {@link ItemTouchHelper} to a recycler view.
         * @param context a valid <b>non-null</b> Context (used to inflate drawables and such).
         * @param recyclerView a valid <b>non-null</b> {@link RecyclerView} to attach the touch helper to.
         * @return a valid instance of {@link ItemTouchHelper} attached to a recyclerView.
         */
        public ItemTouchHelper buildAndAttach(@NonNull Context context, @NonNull RecyclerView recyclerView) {
            RecyclerViewItemSwipeHelper swipeHelper = new RecyclerViewItemSwipeHelper(context, this);
            ItemTouchHelper helper = new ItemTouchHelper(swipeHelper);
            helper.attachToRecyclerView(recyclerView);
            return helper;
        }
    }
}
