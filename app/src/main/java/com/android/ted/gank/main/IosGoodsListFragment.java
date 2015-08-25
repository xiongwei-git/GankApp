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

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;

import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IosGoodsListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ArrayList<Goods> mAllIosGoods;
    private GoodsItemAdapter mIosItemAdapter;

    private Observer<GoodsResult> getIosGoodsObserver = new Observer<GoodsResult>() {
        @Override
        public void onNext(final GoodsResult goodsResult) {
            if (null != goodsResult && null != goodsResult.getResults()) {
                mAllIosGoods.clear();
                mAllIosGoods.addAll(goodsResult.getResults());
                mIosItemAdapter.updateItems(mAllIosGoods,true);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllIosGoods = new ArrayList<>();
        mIosItemAdapter = new GoodsItemAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_cheese_list, container, false);
        setupRecyclerView(mRecyclerView);
        return mRecyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadAllIosGoods();
    }

    private void loadAllIosGoods(){
        GankCloudApi.getIns()
                .getIosGoods(GankCloudApi.LOAD_LIMIT, 1)
                .cache()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getIosGoodsObserver);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(mIosItemAdapter);
    }

}
