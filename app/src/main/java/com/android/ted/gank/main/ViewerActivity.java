/*
 *    Copyright 2015 TedXiong <xiong-wei@hotmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.android.ted.gank.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.ted.gank.R;
import com.android.ted.gank.db.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnPageChange;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ViewerActivity extends AppCompatActivity implements RealmChangeListener {

    private static final String TAG = "ViewerActivity";

    private static final int SYSTEM_UI_BASE_VISIBILITY = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    private static final int SYSTEM_UI_IMMERSIVE = View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.pager)
    ViewPager pager;

    private int index;

    private Realm mRealm;

    private ArrayList<Image> images;

    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportPostponeEnterTransition();

        setContentView(R.layout.activity_viewer);
        ButterKnife.bind(this);

        images = new ArrayList<>();

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        index = getIntent().getIntExtra("index", 0);

        mRealm = Realm.getInstance(this);
        mRealm.addChangeListener(this);

        loadAllImage();

        adapter = new PagerAdapter();

        pager.setAdapter(adapter);
        pager.setCurrentItem(index);

        // 避免图片在进行 Shared Element Transition 时盖过 Toolbar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementsUseOverlay(false);
        }

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Image image = images.get(pager.getCurrentItem());
                ViewerFragment fragment = (ViewerFragment) adapter.instantiateItem(pager, pager.getCurrentItem());

                sharedElements.clear();
                sharedElements.put(image.getUrl(), fragment.getSharedElement());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(this);
        mRealm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onChange() {
        loadAllImage();
        adapter.notifyDataSetChanged();
    }

    private void loadAllImage() {
        RealmResults<Image> results = mRealm.where(Image.class).notEqualTo("width", 0).findAll();
        images.addAll(results);
    }

    @OnPageChange(value = R.id.pager, callback = OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED)
    @SuppressWarnings("unused")
    void onPageChange(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
            hideSystemUi();
        }
    }

    public void toggleToolbar() {
        if (toolbar.getTranslationY() == 0) {
            hideSystemUi();
        } else {
            showSystemUi();
        }
    }

    private void showSystemUi() {
        pager.setSystemUiVisibility(SYSTEM_UI_BASE_VISIBILITY);
        toolbar.animate()
                .translationY(0)
                .setDuration(400)
                .start();
    }

    private void hideSystemUi() {
        pager.setSystemUiVisibility(SYSTEM_UI_BASE_VISIBILITY | SYSTEM_UI_IMMERSIVE);
        toolbar.animate()
                .translationY(-toolbar.getHeight())
                .setDuration(400)
                .start();
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        data.putExtra("index", pager.getCurrentItem());
        setResult(RESULT_OK, data);

        showSystemUi();

        super.supportFinishAfterTransition();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ViewerFragment.newFragment(
                    images.get(position).getUrl(),
                    position == index);
        }

    }

}
