package com.gryzor.swipenolib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextPaint;
import android.text.TextUtils;
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
    private Context context;
    private OnSwipeListener listener;
    private Drawable background;
    private Drawable deleteIcon;
    private int deleteDecorationMargin;
    private int deleteTextSize;
    private boolean initiated;
    private final int[] disabledLocations;
    private boolean disableSwipeOnLastPosition;
    private TextPaint textPaint;
    private Rect textRect;
    private String deleteText;

    private RecyclerViewItemSwipeHelper(final Context context, final Builder builder) {
        super(0, builder.swipeDirs);
        this.context = context;
        this.background = builder.background;

        if (builder.deleteIcon != null) {
            this.deleteIcon = builder.deleteIcon;
            deleteIcon.setColorFilter(builder.deleteDecorationColor, PorterDuff.Mode.SRC_ATOP);
        }

        this.deleteDecorationMargin = builder.deleteDecorationMargin;
        this.listener = builder.listener;
        this.disabledLocations = builder.disabledLocations;
        this.disableSwipeOnLastPosition = builder.disableSwipeOnLastPosition;

        if (!TextUtils.isEmpty(builder.deleteText)) {
            this.deleteTextSize = builder.deleteTextSize;
            this.textRect = new Rect();
            this.deleteText = builder.deleteText;
            this.textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            this.textPaint.setStyle(Paint.Style.FILL);
            this.textPaint.setTextAlign(Paint.Align.LEFT);
            this.textPaint.setColor(builder.deleteDecorationColor);
            this.textPaint.setTextSize(builder.deleteTextSize);
        }
    }

    private void init() {

        // Ensure we have some defaults.
        if (context == null) {
            throw new NullPointerException("You need a valid non-null Context to use this class");
        }

        if (background == null) {
            background = new ColorDrawable(Color.RED);
        }

        if (deleteDecorationMargin < 0) {
            deleteDecorationMargin = (int) context.getResources().getDimension(R.dimen.swipe_cell_delete_image_margin);
        }

        if (deleteTextSize <= 0) {
            deleteTextSize = context.getResources().getDimensionPixelSize(R.dimen.swipe_cell_delete_text_size);
            if (textPaint != null) {
                textPaint.setTextSize(deleteTextSize);
            }
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
        // This would be useful for Drag and Drop, which I didn't implement/support.
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

        if (disabledLocations != null
                && Arrays.binarySearch(disabledLocations, currPos) >= 0
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

        // Draw Icon or Text
        if (deleteIcon != null) {
            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicWidth = deleteIcon.getIntrinsicWidth();
            int intrinsicHeight = deleteIcon.getIntrinsicWidth();

            int iconLeft = itemView.getRight() - deleteDecorationMargin - intrinsicWidth;
            int iconRight = itemView.getRight() - deleteDecorationMargin;
            int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int iconBottom = iconTop + intrinsicHeight;
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(canvas);
        } else if (!TextUtils.isEmpty(deleteText)) {
            int itemHeight = itemView.getBottom() - itemView.getTop();
            textPaint.getTextBounds(deleteText, 0, deleteText.length(), textRect);
            float textX = itemView.getRight() - deleteDecorationMargin - textRect.width();
            float textY = itemView.getTop() + (itemHeight / 2f) + textRect.height() / 2f + textRect.bottom;
            canvas.drawText(deleteText, textX, textY, textPaint);
        }

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
        private String deleteText = null;
        private int deleteDecorationColor = Color.WHITE;
        private int deleteTextSize = -1;
        private int deleteDecorationMargin = -1;
        private OnSwipeListener listener;
        private int[] disabledLocations;
        private boolean disableSwipeOnLastPosition;

        public Builder swipeToStart() {
            swipeDirs |= ItemTouchHelper.START;
            return this;
        }

        public Builder swipeToEnd() {
            swipeDirs |= ItemTouchHelper.END;
            return this;
        }

        public Builder setBackgroundColor(Drawable drawable) {
            this.background = drawable;
            return this;
        }

        public Builder setDeleteImage(Drawable drawable) {
            this.deleteIcon = drawable;
            return this;
        }

        public Builder setDeleteText(String text) {
            this.deleteText = text;
            return this;
        }

        public Builder setDeleteEndMargin(int dp) {
            this.deleteDecorationMargin = dp;
            return this;
        }

        public Builder setDeleteTextSize(int sp) {
            this.deleteTextSize = sp;
            return this;
        }

        public Builder setDeleteDecorationColor(int color) {
            this.deleteDecorationColor = color;
            return this;
        }

        public Builder disableSwipeOnPositions(int... positions) {
            disabledLocations = positions;
            return this;
        }

        public Builder disableSwipeOnLastItem() {
            disableSwipeOnLastPosition = true;
            return this;
        }

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

