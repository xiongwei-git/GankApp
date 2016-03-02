
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
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.android.ted.gank.R;
import com.android.ted.gank.adapter.BenefitGoodsItemAdapter;
import com.android.ted.gank.db.Image;
import com.android.ted.gank.model.Goods;
import com.android.ted.gank.model.GoodsResult;
import com.android.ted.gank.network.GankCloudApi;
import com.android.ted.gank.service.ImageImproveService;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BenefitListFragment extends BaseLoadingFragment implements SwipeRefreshLayout.OnRefreshListener,
        RealmChangeListener {

    @Bind(R.id.benefit_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.benefit_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Image> mAllBenefitImage;
    private BenefitGoodsItemAdapter mBenefitItemAdapter;
    private UpdateResultReceiver updateResultReceiver = new UpdateResultReceiver();
    private Realm mRealm;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    //是否正在更新图片信息
    private boolean bImproveDoing = false;
    private boolean isALlLoad = false;
    private int hasLoadPage = 0;
    private boolean isLoadMore = false;


    private Observer<GoodsResult> getBenefitGoodsObserver = new Observer<GoodsResult>() {
        @Override
        public void onNext(final GoodsResult goodsResult) {
            if(mAllBenefitImage.isEmpty() && goodsResult.getResults().isEmpty()){
                showNoDataView();
                return;
            }
            showContent();
            if(goodsResult.getResults().size() == GankCloudApi.LOAD_LIMIT){
                hasLoadPage++;
            }else {
                isALlLoad = true;
            }
            if (analysisNewImage(goodsResult))
                doImproveJob();
            else refreshBenefitGoods();
        }

        @Override
        public void onCompleted() {
            isLoadMore = false;
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onError(final Throwable error) {
            if (error instanceof RetrofitError) {
                Drawable errorDrawable = new IconDrawable(getContext(), Iconify.IconValue.zmdi_network_off)
                        .colorRes(android.R.color.white);
                RetrofitError e = (RetrofitError) error;
                if (e.getKind() == RetrofitError.Kind.NETWORK) {
                    showError(errorDrawable,"网络异常","好像您的网络出了点问题","重试",mErrorRetryListener);
                } else if (e.getKind() == RetrofitError.Kind.HTTP) {
                    showError(errorDrawable,"服务异常","好像服务器出了点问题","再试一次",mErrorRetryListener);
                } else {
                    showError(errorDrawable,"莫名异常","外星人进攻地球了？","反击",mErrorRetryListener);
                }
            }
        }
    };

    private View.OnClickListener mErrorRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            reloadData();
        }
    };

    @Override
    public void onRefresh() {
        reloadData();
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
    View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_benifit_list,null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupBaseView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshBenefitGoods();
        reloadData();
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
    public void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(this);
        mRealm.close();
    }

    private void refreshBenefitGoods(){
        mAllBenefitImage.clear();
        RealmResults<Image> results = mRealm.where(Image.class).notEqualTo("width",0).findAll();
        mAllBenefitImage.addAll(results);
        mBenefitItemAdapter.replaceWith(mAllBenefitImage);
    }

    private void setupBaseView() {
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        mRecyclerView.setAdapter(mBenefitItemAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    BenefitListFragment.this.onScrollStateChanged();
                }
            }
        });
    }

    private void onScrollStateChanged(){
        int[] positions = new int[mStaggeredGridLayoutManager.getSpanCount()];
        mStaggeredGridLayoutManager.findLastVisibleItemPositions(positions);
        for (int position : positions) {
            if (position == mStaggeredGridLayoutManager.getItemCount() - 1) {
                loadMore();
                break;
            }
        }
    }

    private void loadMore(){
        if(isALlLoad){
            Toast.makeText(getActivity(), "全部加载完毕", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isLoadMore)return;
        isLoadMore = true;
        loadData(hasLoadPage + 1);
    }

    private void reloadData(){
        mSwipeRefreshLayout.setRefreshing(true);
        mAllBenefitImage.clear();
        isALlLoad = false;
        hasLoadPage = 0;
        loadData(1);
    }

    private void loadData(int startPage){
        GankCloudApi.getIns()
                .getBenefitsGoods(GankCloudApi.LOAD_LIMIT, startPage)
                .cache()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getBenefitGoodsObserver);
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
                Image image = Image.queryImageById(mRealm, goods.get_id());
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

    private void showNoDataView(){
        Drawable emptyDrawable = new IconDrawable(getContext(), Iconify.IconValue.zmdi_shopping_cart)
                .colorRes(android.R.color.white);
        List<Integer> skipIds = new ArrayList<>();
        showEmpty(emptyDrawable, "数据列表为空", "没有拿到数据哎，请等一下再来玩妹子吧", skipIds);
    }

    private void startViewerActivity(View itemView, int position) {
        Intent intent = new Intent(getActivity(), ViewerActivity.class);
        intent.putExtra("index", position);
//        ActivityOptionsCompat options = ActivityOptionsCompat
//                .makeSceneTransitionAnimation(getActivity(), itemView, mBenefitItemAdapter.get(position).getUrl());
        getActivity().startActivity(intent);
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

//    public Map<String, View> getActivitySharedElements(int position,Map<String,View> map){
//        map.put(mBenefitItemAdapter.get(position).getUrl(),mStaggeredGridLayoutManager.findViewByPosition(position));
//        return map;
//    }
//
//    public void onActivityReenter(Bundle bundle){
//        mRecyclerView.scrollToPosition(bundle.getInt("index", 0));
//        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//                mRecyclerView.requestLayout();
//                getActivity().supportStartPostponedEnterTransition();
//                return true;
//            }
//        });
//    }
}
