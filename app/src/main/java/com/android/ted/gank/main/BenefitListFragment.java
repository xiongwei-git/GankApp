
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.ted.gank.R;
import com.android.ted.gank.adapter.BenefitGoodsItemAdapter;
import com.android.ted.gank.db.Image;
import com.android.ted.gank.model.Goods;
import com.android.ted.gank.model.GoodsResult;
import com.android.ted.gank.network.GankCloudApi;
import com.android.ted.gank.service.ImageImproveService;

import java.util.ArrayList;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BenefitListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        RealmChangeListener {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Image> mAllBenefitImage;
    private BenefitGoodsItemAdapter mBenefitItemAdapter;
    private UpdateResultReceiver updateResultReceiver = new UpdateResultReceiver();
    private Realm mRealm;
    //是否正在更新图片信息
    private boolean bImproveDoing = false;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;


    private Observer<GoodsResult> getBenefitGoodsObserver = new Observer<GoodsResult>() {
        @Override
        public void onNext(final GoodsResult goodsResult) {
            if (analysisNewImage(goodsResult))
                doImproveJob();
        }

        @Override
        public void onCompleted() {
            mSwipeRefreshLayout.setRefreshing(false);
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
    public void onRefresh() {
        reloadBenefitGoods();
    }

    @Override
    public void onChange() {
        if(!bImproveDoing)return;
        if(!this.isVisible())return;
        refreshBenefitGoods();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllBenefitImage = new ArrayList<>();
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mBenefitItemAdapter = new BenefitGoodsItemAdapter(getActivity()) {
            @Override
            protected void onItemClick(View v, int position) {
                startViewerActivity(v,position);
            }
        };
        mRealm = Realm.getInstance(getActivity());
        mRealm.addChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_benifit_list, container, false);
        mRecyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.benefit_recycler_view);
        setupBaseView();
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshBenefitGoods();
        reloadBenefitGoods();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(updateResultReceiver,
                new IntentFilter(ImageImproveService.ACTION_UPDATE_RESULT),
                ImageImproveService.PERMISSION_ACCESS_UPDATE_RESULT, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(updateResultReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.removeChangeListener(this);
        mRealm.close();
    }

    private void reloadBenefitGoods() {
        GankCloudApi.getIns()
                .getBenefitsGoods(20, GankCloudApi.LOAD_START)
                .cache()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getBenefitGoodsObserver);
    }

    private void refreshBenefitGoods(){
        mAllBenefitImage.clear();
        RealmResults<Image> results = mRealm.where(Image.class).notEqualTo("width",0).findAll();
        mAllBenefitImage.addAll(results);
        mBenefitItemAdapter.updateItems(mAllBenefitImage, true);
    }

    private void setupBaseView() {
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        mRecyclerView.setAdapter(mBenefitItemAdapter);
    }

    /***
     * 分析新的数据
     *
     * @param goodsResult
     * @return 是否有新数据插入
     */
    private boolean analysisNewImage(final GoodsResult goodsResult) {
        mRealm.beginTransaction();
        if (null != goodsResult && null != goodsResult.getResults()) {
            for (Goods goods : goodsResult.getResults()) {
                Image image = Image.queryImageById(mRealm, goods.getObjectId());
                if(null == image)image = mRealm.createObject(Image.class);
                Image.updateDbGoods(image,goods);
            }
            mRealm.commitTransaction();
            return true;
        }
        mRealm.cancelTransaction();
        return false;
    }


    private void doImproveJob() {
        bImproveDoing = true;
        Intent intent = new Intent(getActivity(), ImageImproveService.class);
        intent.setAction(ImageImproveService.ACTION_IMPROVE_IMAGE);
        getActivity().startService(intent);
    }

    private void startViewerActivity(View itemView, int position) {
        Intent intent = new Intent(getActivity(), ViewerActivity.class);
        intent.putExtra("index", position);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(getActivity(), itemView, mAllBenefitImage.get(position).getUrl());
        getActivity().startActivity(intent, options.toBundle());
    }

    private class UpdateResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            bImproveDoing = false;
            int count = intent.getIntExtra(ImageImproveService.EXTRA_CHANGE, 0);
            if(count > 0)
                refreshBenefitGoods();
        }
    }

    public Map<String, View> getActivitySharedElements(int position,Map<String,View> map){
        map.put(mAllBenefitImage.get(position).getUrl(),mStaggeredGridLayoutManager.findViewByPosition(position));
        return map;
    }

    public void onActivityReenter(Bundle bundle){
        mRecyclerView.scrollToPosition(bundle.getInt("index", 0));
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                mRecyclerView.requestLayout();
                getActivity().supportStartPostponedEnterTransition();
                return true;
            }
        });
    }
}
