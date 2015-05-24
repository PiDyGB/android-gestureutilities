/*
 * Copyright 2013 Google Inc
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
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * A {@link View.OnTouchListener} and a {@link android.support.v7.widget.RecyclerView.OnScrollListener}
 * that makes the list items in a {@link android.support.v7.widget.RecyclerView} dismissable.
 * <p/>
 * <p>If a scroll listener and/or a touch listener are already assigned, the caller should still pass scroll and touch changes through to this listener.
 * This will ensure that this {@link SwipeDismissViewListener} is paused during list view scrolling.</p>
 * <p/>
 * <p>Example usage:</p>
 * <p/>
 * <pre>
 * SwipeDismissViewListener swipeDismissViewTouchListener = new SwipeDismissViewListener(context,
 *              new SwipeDismissViewListener.OnSwipeDismissListener() {
 *                  public boolean canDismiss(View view) {
 *                      return true;
 *                  }
 *
 *                  public void onDismissStart(View view) {
 *                      // do something
 *                  }
 *
 *                  public void onDismissCancel(View view){
 *                      // do something
 *                  }
 *
 *                  public void onDismissEnd(View view, boolean dismissRight){
 *                      // do something
 *                  }
 *
 *               mView.setOnTouchListener(swipeDismissViewTouchListener);
 * </pre>
 * <p/>
 */
@SuppressWarnings("unused")
public class SwipeDismissViewListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private OnSwipeDismissListener mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private float mX;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private boolean mPaused;
    private boolean mDismissCallbackCalled;

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given view.
     *
     * @param context   A context
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     *                  dismiss the view.
     */
    public SwipeDismissViewListener(Context context, OnSwipeDismissListener callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mCallbacks = callbacks;
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent motionEvent) {
        boolean consumed = false;
        if (mViewWidth < 2) {
            mViewWidth = view.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }


                mX = motionEvent.getRawX();
                if (mCallbacks.canDismiss(view)) {
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    return false;
                }

                if (mSwiping) {
                    // cancel
                    animate(view).translationX(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mCallbacks.onDismissCancel(view);
                                    mDismissCallbackCalled = false;
                                }
                            });
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mX = 0;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    return false;
                }

                float deltaX = motionEvent.getRawX() - mX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }
                if (dismiss) {
                    // dismiss
                    final boolean finalDismissRight = dismissRight;
                    animate(view)
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {

                                    mCallbacks.onDismissEnd(view, finalDismissRight);
                                    mDismissCallbackCalled = false;

                                    setTranslationX(view, 0);
                                }
                            });
                    consumed = true;
                } else if (mSwiping) {
                    // cancel
                    animate(view)
                            .translationX(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mCallbacks.onDismissCancel(view);
                                    mDismissCallbackCalled = false;
                                }
                            });
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mX = 0;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    return false;
                }
                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mX;
                if (Math.abs(deltaX) > mSlop) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    view.getParent().requestDisallowInterceptTouchEvent(true);

                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    view.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }
                if (mSwiping) {
                    setTranslationX(view, deltaX - mSwipingSlop);
                }
                if (mSwiping && !mDismissCallbackCalled) {
                    mCallbacks.onDismissStart(view);
                    mDismissCallbackCalled = true;
                }
                if (mSwiping)
                    consumed = true;
                break;
            }
        }
        return consumed;
    }

    /**
     * The callback interface used by {@link SwipeDismissViewListener} to inform its client
     * about a successful dismissal of one or more list item positions.
     */
    public interface OnSwipeDismissListener {
        /**
         * Called to determine whether the view can be dismissed.
         */
        boolean canDismiss(View view);

        /**
         * Called to determine whether the user starts the dismiss gesture
         *
         * @param view The {@link View} to dismiss
         */
        void onDismissStart(View view);

        /**
         * Called to determine whether the user cancels the dismiss gesture
         *
         * @param view The {@link View} to dismiss
         */
        void onDismissCancel(View view);

        /**
         * Called when the user has indicated they she would like to dismiss the view
         *
         * @param view         The originating {@link View}.
         * @param dismissRight True if the view is dimissed to right
         */
        void onDismissEnd(View view, boolean dismissRight);
    }
}