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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.pidygb.gestureutilities.ScrollToHideRecyclerViewListener;
import com.github.pidygb.gestureutilities.SwipeDismissViewListener;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements ScrollToHideRecyclerViewListener.OnScrollToHideCallback, View.OnClickListener {

    private View mButton;
    private boolean mButtonAnimating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.button);

        final View textToSwipe = findViewById(R.id.text_to_swipe);

        SwipeDismissViewListener swipeDismissViewListener = new SwipeDismissViewListener(this, new SwipeDismissViewListener.OnSwipeDismissListener() {
            @Override
            public boolean canDismiss(View view) {
                return true;
            }

            @Override
            public void onDismissStart(View view, boolean dismissRight) {
                if (BuildConfig.DEBUG) Log.d(MainActivity.class.getSimpleName(), "onDismissStart: " + dismissRight);
            }

            @Override
            public void onDismissCancel(View view) {

            }

            @Override
            public void onDismissEnd(View view, boolean dismissRight) {
                textToSwipe.setVisibility(View.INVISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textToSwipe.setVisibility(View.VISIBLE);
                    }
                }, 5000);
            }
        });

        textToSwipe.setOnTouchListener(swipeDismissViewListener);
        textToSwipe.setOnClickListener(this);

        String[] stars = {"Andromeda", "Antlia", "Apus", "Aquarius", "Aquila", "Ara", "Aries", "Auriga", "Boötes", "Caelum", "Camelopardalis", "Cancer", "Canes Venatici", "Canis Major", "Canis Minor", "Capricornus", "Carina", "Cassiopeia", "Centaurus", "Cepheus", "Cetus", "Chamaeleon", "Circinus", "Columba", "Coma Berenices", "Corona Australis", "Corona Borealis", "Corvus", "Crater", "Crux", "Cygnus", "Delphinus", "Dorado", "Draco", "Equuleus", "Eridanus", "Fornax", "Gemini", "Grus", "Hercules", "Horologium", "Hydra", "Hydrus", "Indus", "Lacerta", "Leo", "Leo Minor", "Lepus", "Libra", "Lupus", "Lynx", "Lyra", "Mensa", "Microscopium", "Monoceros", "Musca", "Norma", "Octans", "Ophiuchus", "Orion", "Pavo", "Pegasus", "Perseus", "Phoenix", "Pictor", "Pisces", "Piscis Austrinus", "Puppis", "Pyxis", "Reticulum", "Sagitta", "Sagittarius", "Scorpius", "Sculptor", "Scutum", "Serpens", "Sextans", "Taurus", "Telescopium", "Triangulum", "Triangulum Australe", "Tucana", "Ursa Major", "Ursa Minor", "Vela", "Virgo", "Volans", "Vulpecula"};


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        ScrollToHideRecyclerViewListener scrollToHideRecyclerViewListener = new ScrollToHideRecyclerViewListener(this, linearLayoutManager, this);

        recyclerView.setLayoutManager(linearLayoutManager);
        SimpleAdapter mAdapter = new SimpleAdapter(this, new ArrayList<>(Arrays.asList(stars)));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.setOnTouchListener(scrollToHideRecyclerViewListener);
        recyclerView.addOnScrollListener(scrollToHideRecyclerViewListener);

    }

    public void showButton(final boolean show) {
        // Check if the fab button is showed and and a new show is requested
        if (show && mButton.isShown())
            return;
        if (!show && !mButton.isShown())
            // Check if the fab button is hide and a new hide is requested
            return;
        // Avoid to start a new animation if it's current running
        if (!mButtonAnimating) {
            // Calculate the translation y distance
            final float tranY = mButton.getWidth() + ((RelativeLayout.LayoutParams) mButton.getLayoutParams()).bottomMargin;

            ViewCompat.animate(mButton)
                    .translationY((show) ? 0 : tranY)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            mButtonAnimating = false;
                            // if the request is to hide set the visibility at gone
                            if (!show)
                                mButton.setVisibility(View.GONE);
                            // Clear the animation, otherwise the view is clickable in old android version
                            mButton.clearAnimation();
                        }
                    });
            // if the request is to show  translate the view to the start position
            // for the show animation
            // (the previous clear animation sets the view to the original position)
            if (show) {
                ViewCompat.setTranslationY(mButton, tranY);
                mButton.setVisibility(View.VISIBLE);
            }
            mButtonAnimating = true;
        }
    }

    @Override
    public void hide(boolean hide) {
        showButton(!hide);
    }

    @Override
    public boolean canHide() {
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://en.wikipedia.org/wiki/Lists_of_stars_by_constellation"));
        startActivity(intent);
    }
}
