/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ted.gank.bak;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.ted.gank.R;
import com.android.ted.gank.model.DayGoodsResult;
import com.android.ted.gank.network.GankCloudApi;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CheeseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "cheese_name";
    private GankCloudApi mGankCloudApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra(EXTRA_NAME);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);

        loadBackdrop();
        mGankCloudApi = new GankCloudApi();
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        BitmapPool pool = Glide.get(this).getBitmapPool();
        Glide.with(this).load(Cheeses.getRandomCheeseDrawable()).centerCrop().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mGankCloudApi.getGoodsByDay(2015,8,10).cache().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getAndroidGoodsObserver);
        return super.onOptionsItemSelected(item);
    }

    private Observer<DayGoodsResult> getAndroidGoodsObserver = new Observer<DayGoodsResult>() {
        @Override
        public void onNext(final DayGoodsResult goodsResult) {
            if(null != goodsResult && null != goodsResult.getResults()){
                //mImageListInfo = imageListInfoResults.getResults().get(0);
            }
        }

        @Override
        public void onCompleted() {
            //getNewPhotos();
        }

        @Override
        public void onError(final Throwable error) {
            Toast.makeText(CheeseDetailActivity.this,"onError",Toast.LENGTH_SHORT).show();
//            if (error instanceof RetrofitError) {
//                RetrofitError e = (RetrofitError) error;
//                if (e.getKind() == RetrofitError.Kind.NETWORK) {
//                    mImagesErrorView.setErrorTitle(R.string.error_network);
//                    mImagesErrorView.setErrorSubtitle(R.string.error_network_subtitle);
//                } else if (e.getKind() == RetrofitError.Kind.HTTP) {
//                    mImagesErrorView.setErrorTitle(R.string.error_server);
//                    mImagesErrorView.setErrorSubtitle(R.string.error_server_subtitle);
//                } else {
//                    mImagesErrorView.setErrorTitle(R.string.error_uncommon);
//                    mImagesErrorView.setErrorSubtitle(R.string.error_uncommon_subtitle);
//                }
//            }
//
//            mImagesProgress.setVisibility(View.GONE);
//            mImageRecycler.setVisibility(View.GONE);
//            mImagesErrorView.setVisibility(View.VISIBLE);
//
//            mImagesErrorView.setOnRetryListener(new RetryListener() {
//                @Override
//                public void onRetry() {
//                    getImageListInfo();
//                }
//            });
        }
    };
}
