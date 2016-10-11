package com.github.pidygb.gestureutilities.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.github.pidygb.gestureutilities.SwipeDismissViewListener;

/**
 * SwipeDismissViewHolder
 * Created by Giuseppe Buzzanca (PiDy) on 11/10/16.
 */

public abstract class SwipeDismissViewHolder extends RecyclerView.ViewHolder implements SwipeDismissViewListener.OnSwipeDismissListener {

    public SwipeDismissViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public boolean canDismiss(View view) {
        return true;
    }

}
