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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.ted.gank.R;
import com.android.ted.gank.adapter.GoodsItemAdapter;
import com.android.ted.gank.db.Image;
import com.android.ted.gank.model.Goods;
import com.android.ted.gank.model.GoodsResult;
import com.android.ted.gank.network.GankCloudApi;
import com.android.ted.gank.service.ImageImproveService;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BenefitListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        RealmChangeListener {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Goods> mAllBenefitGoods;
    private GoodsItemAdapter mBenefitItemAdapter;
    private Realm mRealm;

    private Observer<GoodsResult> getBenefitGoodsObserver = new Observer<GoodsResult>() {
        @Override
        public void onNext(final GoodsResult goodsResult) {
            analysisNewImage(goodsResult);
        }

        @Override
        public void onCompleted() {
            mSwipeRefreshLayout.setRefreshing(false);
            doImproveJob();
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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllBenefitGoods = new ArrayList<>();
        mBenefitItemAdapter = new GoodsItemAdapter(getActivity());
        mRealm = Realm.getInstance(getActivity());
        mRealm.addChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_benifit_list, container, false);
        mRecyclerView = (RecyclerView)mSwipeRefreshLayout.findViewById(R.id.benefit_recycler_view);
        setupBaseView();
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reloadBenefitGoods();
    }

    private void reloadBenefitGoods(){
        GankCloudApi.getIns()
                .getBenefitsGoods(GankCloudApi.LOAD_LIMIT, GankCloudApi.LOAD_STRAT)
                .cache()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getBenefitGoodsObserver);
    }

    private void setupBaseView() {
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary,R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setAdapter(mBenefitItemAdapter);
    }

    /***
     * 分析新的数据
     * @param goodsResult
     * @return 是否有新数据插入
     */
    private boolean analysisNewImage(final GoodsResult goodsResult){
        if (null != goodsResult && null != goodsResult.getResults()) {
            int count = 0;
            for (Goods goods:goodsResult.getResults()){
                Image image = Image.queryOrCreate(mRealm, goods);
                insertImage(image);
            }


            mAllBenefitGoods.clear();
            mAllBenefitGoods.addAll(goodsResult.getResults());
            mBenefitItemAdapter.updateItems(mAllBenefitGoods,true);
        }
        return false;
    }

    private void insertImage(Image image){
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(image);
        mRealm.commitTransaction();
    }



    private void doImproveJob(){
        Intent intent = new Intent(getActivity(), ImageImproveService.class);
        intent.setAction(ImageImproveService.ACTION_IMPROVE_IMAGE);
        getActivity().startService(intent);
    }

}
