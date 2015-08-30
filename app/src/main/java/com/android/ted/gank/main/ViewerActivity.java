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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.android.ted.gank.R;
import com.android.ted.gank.db.Image;
import com.umeng.analytics.MobclickAgent;

import net.grandcentrix.tray.TrayAppPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnPageChange;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ViewerActivity extends AppCompatActivity implements RealmChangeListener,ViewPager.OnPageChangeListener{
    public static final int REQUEST_CODE_SET_WALLPAPER = 0x1001;
    public static final String KEY_SET_WALLPAPER_TIPS = "key_set_wallpaper_tips";

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

    private TrayAppPreferences mAppPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        ButterKnife.bind(this);
        mAppPreferences = new TrayAppPreferences(this);
        images = new ArrayList<>();
        mRealm = Realm.getInstance(this);
        mRealm.addChangeListener(this);
        loadAllImage();

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        index = getIntent().getIntExtra("index", 0);

        adapter = new PagerAdapter();

        pager.setAdapter(adapter);
        pager.setCurrentItem(index);
        pager.addOnPageChangeListener(this);

        // 避免图片在进行 Shared Element Transition 时盖过 Toolbar
//        if (Build.VERSION.SDK_INT >= 21) {
//            getWindow().setSharedElementsUseOverlay(false);
//        }
//
//        setEnterSharedElementCallback(new SharedElementCallback() {
//            @Override
//            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                Image image = images.get(pager.getCurrentItem());
//                ViewerFragment fragment = (ViewerFragment) adapter.instantiateItem(pager, pager.getCurrentItem());
//
//                sharedElements.clear();
//                sharedElements.put(image.getUrl(), fragment.getSharedElement());
//            }
//        });
        hideSystemUi();
        showTips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(this);
        mRealm.close();
    }

    @Override
    public void onChange() {
        loadAllImage();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
            hideSystemUi();
        }
    }


    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private void loadAllImage() {
        RealmResults<Image> results = mRealm.where(Image.class).notEqualTo("width", 0).findAll();
        images.addAll(results);
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

    private void showTips(){
        if(mAppPreferences.getBoolean(KEY_SET_WALLPAPER_TIPS,true)){
            Snackbar.make(toolbar,"长按妹子图即可设置为壁纸",Snackbar.LENGTH_LONG).setAction("知道了", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAppPreferences.put(KEY_SET_WALLPAPER_TIPS,false);
                }
            }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_SET_WALLPAPER)
                Toast.makeText(this,"壁纸设置成功",Toast.LENGTH_SHORT).show();
        }
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
            return ViewerFragment.newFragment(images.get(position).getUrl(), position == index);
        }
    }

}
