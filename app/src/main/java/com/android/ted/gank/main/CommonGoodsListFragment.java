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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.ted.gank.R;
import com.android.ted.gank.adapter.GoodsItemAdapter;
import com.android.ted.gank.model.Goods;
import com.android.ted.gank.model.GoodsResult;
import com.android.ted.gank.network.GankCloudApi;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CommonGoodsListFragment extends BaseLoadingFragment implements SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.common_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.common_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Goods> mAllCommonGoods;
    private GoodsItemAdapter mCommonItemAdapter;
    private int lastVisibleItem;
    private boolean isALlLoad = false;
    private int hasLoadPage = 0;
    private boolean isLoadMore = false;
    private String mType = "Android";

    public static CommonGoodsListFragment newFragment(String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        CommonGoodsListFragment fragment = new CommonGoodsListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Observer<GoodsResult> getCommonGoodsObserver = new Observer<GoodsResult>() {
        @Override
        public void onNext(final GoodsResult goodsResult) {
            if (null != goodsResult && null != goodsResult.getResults()) {
                disposeResults(goodsResult);
            }
        }

        @Override
        public void onCompleted() {
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
            isLoadMore = false;
        }
    };

    private View.OnClickListener mErrorRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            reloadData();
        }
    };

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == mCommonItemAdapter.getItemCount()) {
                mSwipeRefreshLayout.setRefreshing(true);
                loadMore();
            }
        }
    };

    @Override
    public void onRefresh() {
        reloadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getString("type","common");
        mAllCommonGoods = new ArrayList<>();
        mCommonItemAdapter = new GoodsItemAdapter(getActivity());
    }

    @Override
    View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_common_list,null);
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
        showLoading();
        reloadData();
    }

    private void setupBaseView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setAdapter(mCommonItemAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void loadMore(){
        if(isLoadMore)return;
        if(isALlLoad){
            Snackbar.make(mRecyclerView,"全部加载完毕",Snackbar.LENGTH_SHORT)
                    .setAction("知道了",null)
                    .show();
            return;
        }
        isLoadMore = true;
        loadData(hasLoadPage + 1);
    }

    private void reloadData(){
        mSwipeRefreshLayout.setRefreshing(true);
        mAllCommonGoods.clear();
        isALlLoad = false;
        hasLoadPage = 0;
        loadData(1);
    }

    private void loadData(int startPage){
        GankCloudApi.getIns()
                .getCommonGoods(mType, GankCloudApi.LOAD_LIMIT, startPage)
                .cache()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getCommonGoodsObserver);
    }

    private void disposeResults(final GoodsResult goodsResult){
        if(mAllCommonGoods.isEmpty() && goodsResult.getResults().isEmpty()){
            showNoDataView();
            return;
        }
        showContent();
        if(goodsResult.getResults().size() == GankCloudApi.LOAD_LIMIT){
            hasLoadPage++;
        }else {
            isALlLoad = true;
        }
        isLoadMore = false;
        mAllCommonGoods.addAll(goodsResult.getResults());
        mCommonItemAdapter.updateItems(mAllCommonGoods, hasLoadPage == 1);
    }

    private void showNoDataView(){
        Drawable emptyDrawable = new IconDrawable(getContext(), Iconify.IconValue.zmdi_shopping_cart)
                .colorRes(android.R.color.white);
        List<Integer> skipIds = new ArrayList<>();
        showEmpty(emptyDrawable, "数据列表为空", "没有拿到数据哎，请等一下再来玩干货吧", skipIds);
    }
}
