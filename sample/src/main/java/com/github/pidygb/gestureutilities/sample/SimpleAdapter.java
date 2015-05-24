/*
 * Copyright (C) 2015 Giuseppe Buzzanca
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.pidygb.gestureutilities.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pidygb.gestureutilities.SwipeDismissViewListener;

import java.util.ArrayList;

/**
 * SimpleAdapter
 * <p/>
 * Created by Giuseppe Buzzanca on 24/05/15.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {

    private final ArrayList<String> mDataSet;
    private final Context mContext;

    public SimpleAdapter(Context context, ArrayList<String> dataset) {
        mContext = context;
        mDataSet = dataset;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_textview, parent, false);
        return new SimpleViewHolder(this, v);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        holder.mTextView.setText(mDataSet.get(position));
        holder.mTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    private void remove(String star) {
        int position = mDataSet.indexOf(star);
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener, SwipeDismissViewListener.OnSwipeDismissListener {
        private final TextView mTextView;
        private final SwipeDismissViewListener mSwipeDismissViewListener;
        private final SimpleAdapter mAdapter;

        SimpleViewHolder(SimpleAdapter adapter, View view) {
            super(view);
            mAdapter = adapter;
            mTextView = (TextView) view;
            mTextView.setOnTouchListener(this);
            mTextView.setOnClickListener(this);
            mSwipeDismissViewListener = new SwipeDismissViewListener(view.getContext(), this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://en.wikipedia.org/wiki/List_of_stars_in_" + mTextView.getText()));
            mAdapter.mContext.startActivity(intent);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mSwipeDismissViewListener.onTouch(v, event);
        }

        @Override
        public boolean canDismiss(View view) {
            return true;
        }

        @Override
        public void onDismissStart(View view) {

        }

        @Override
        public void onDismissCancel(View view) {

        }

        @Override
        public void onDismissEnd(View view, boolean dismissRight) {
            mTextView.setVisibility(View.INVISIBLE);
            mAdapter.remove(mTextView.getText().toString());
        }
    }
}
