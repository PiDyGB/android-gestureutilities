/*
 * Copyright 2015 Giuseppe Buzzanca (giuseppebuzzanca@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pidygb.gestureutilities;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * A {@link View.OnTouchListener} and a {@link android.support.v7.widget.RecyclerView.OnScrollListener}
 * that callbacks when hide or show something syncronized the {@link android.support.v7.widget.RecyclerView} scrolling.
 * <p/>
 * <p>If a scroll listener and/or a touch listener are already assigned, the caller should still pass scroll and touch changes through to this listener.
 * This will ensure that this scroll listener is paused during list view scrolling.</p>
 * <p/>
 * <p/>
 */
@SuppressWarnings("unused")
public class ScrollToHideRecyclerViewListener extends RecyclerView.OnScrollListener implements View.OnTouchListener {

    private final LinearLayoutManager mLinearLayoutManager;
    private final int mTouchSlop;
    private boolean mIsScrolling;
    private float mStartY;
    private OnScrollToHideCallback mOnScrollToHideCallback;
    private boolean mDisableHideView;
    private View.OnTouchListener mOnTouchListener;
    private RecyclerView.OnScrollListener mRecyclerScrollListener;

    public ScrollToHideRecyclerViewListener(Context context, LinearLayoutManager linearLayoutManager, OnScrollToHideCallback callback) {
        mLinearLayoutManager = linearLayoutManager;
        mOnScrollToHideCallback = callback;
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    public void setOnScrollListner(RecyclerView.OnScrollListener onScrollListner) {
        mRecyclerScrollListener = onScrollListner;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOnTouchListener != null)
            if (mOnTouchListener.onTouch(v, event))
                return true;

        if (mDisableHideView)
            return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // Sets the start y position
                mIsScrolling = false;
                mStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Check scrolling direction only if the view is scrolling
                if (mIsScrolling) {
                    if (mStartY < event.getY()) {
                        // Scroll down, show the view
                        if (mOnScrollToHideCallback != null && mOnScrollToHideCallback.canHide())
                            mOnScrollToHideCallback.hide(false);
                    } else {
                        // Scroll up, hide the view
                        if (mOnScrollToHideCallback != null && mOnScrollToHideCallback.canHide())
                            mOnScrollToHideCallback.hide(true);
                    }
                    mStartY = event.getY();
                    break;
                }

                //
                final float yDiff = Math.abs(event.getY() - mStartY);

                // If scrolling more than the touch slop, start the scroll
                if (yDiff > mTouchSlop) {
                    mIsScrolling = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsScrolling = false;
                mStartY = 0;
                break;
        }
        return false;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (mRecyclerScrollListener != null)
            mRecyclerScrollListener.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (mRecyclerScrollListener != null)
            mRecyclerScrollListener.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = mLinearLayoutManager.getChildCount();
        int totalItemCount = mLinearLayoutManager.getItemCount();
        int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        super.onScrolled(recyclerView, dx, dy);
        if (firstVisibleItem == 0 && (visibleItemCount <= totalItemCount)) {
            // All items are showed in a single page check the bottom of the last item
            int bottomLastItem = (recyclerView.getChildAt(visibleItemCount - 1) != null) ? recyclerView.getChildAt(visibleItemCount - 1).getBottom() : 0;
            int bottomListView = recyclerView.getBottom();
            if (bottomLastItem > bottomListView) {
                mDisableHideView = false;
            } else {
                mDisableHideView = true;
                if (mOnScrollToHideCallback != null && mOnScrollToHideCallback.canHide())
                    mOnScrollToHideCallback.hide(false);
            }
        } else {
            mDisableHideView = false;
        }
    }

    /**
     * The callback interface used by {@link ScrollToHideRecyclerViewListener}
     * to inform its client to hide or not something because the {@link android.support.v7.widget.RecyclerView} page scrolls.
     */
    public interface OnScrollToHideCallback {

        /**
         * Show or hide
         */
        void hide(boolean hide);

        /**
         * Enable or disable hide function
         */
        boolean canHide();

    }

}
