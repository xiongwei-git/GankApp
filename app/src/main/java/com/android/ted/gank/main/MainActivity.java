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

package com.android.ted.gank.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.ted.gank.R;
import com.android.ted.gank.adapter.MainFragmentPagerAdapter;
import com.android.ted.gank.config.Constants;
import com.android.ted.gank.data.ImageGoodsCache;
import com.android.ted.gank.db.Image;
import com.android.ted.gank.model.GoodsResult;
import com.android.ted.gank.network.GankCloudApi;

import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Realm mRealm;
    private Bundle mReenterState;
    private MainFragmentPagerAdapter mPagerAdapter;
    private BenefitListFragment mBenefitListFragment;

    /***
     * 获取福利图的回调接口，拿到数据用来做背景
     */
    private Observer<GoodsResult> getImageGoodsObserver = new Observer<GoodsResult>() {
        @Override
        public void onNext(final GoodsResult goodsResult) {
            if (null != goodsResult && null != goodsResult.getResults()) {
                ImageGoodsCache.getIns().addAllImageGoods(goodsResult.getResults());
            }
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(final Throwable error) {
            if (error instanceof RetrofitError) {
                RetrofitError e = (RetrofitError) error;
                if (e.getKind() == RetrofitError.Kind.NETWORK) {
                } else if (e.getKind() == RetrofitError.Kind.HTTP) {
                } else {
                }
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRealm = Realm.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setExitSharedElementCallback(mSharedElementCallback);

        loadAllImageGoods();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void setupViewPager(ViewPager viewPager) {
        mBenefitListFragment = new BenefitListFragment();
        mPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(CommonGoodsListFragment.newFragment("Android"), "Android");
        mPagerAdapter.addFragment(CommonGoodsListFragment.newFragment("IOS"), "IOS");
        mPagerAdapter.addFragment(mBenefitListFragment, "福利");
        viewPager.setAdapter(mPagerAdapter);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        disposeMenuAction(menuItem);
                        return true;
                    }
                });
    }

    private void disposeMenuAction(MenuItem item){
        switch (item.getItemId()){
            case R.id.nav_collect:
            case R.id.nav_time:
                Toast.makeText(this,"功能开发中",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_code:
                callWebview(Constants.GITHUB_URL);
                break;
            case R.id.nav_author:
                callWebview(Constants.AUTHOR_URL);
                break;
        }
    }

    private void loadAllImageGoods() {
        RealmResults<Image> allImage = mRealm.where(Image.class).findAll();
        if (allImage.size() == 0) {
            GankCloudApi.getIns()
                    .getBenefitsGoods(GankCloudApi.LOAD_LIMIT, 1)
                    .cache()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getImageGoodsObserver);
        } else {
            ImageGoodsCache.getIns().addAllImageGoods(allImage);
        }
    }

    private SharedElementCallback mSharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mReenterState != null) {
                int i = mReenterState.getInt("index", 0);
                sharedElements.clear();
                mBenefitListFragment.getActivitySharedElements(i,sharedElements);
                mReenterState = null;
            }
        }
    };

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        supportPostponeEnterTransition();
        mReenterState = new Bundle(data.getExtras());
        mBenefitListFragment.onActivityReenter(new Bundle(data.getExtras()));
    }

    private void callWebview(String url){
        Intent intent= new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
    }
}
