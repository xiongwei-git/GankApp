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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.ted.gank.R;
import com.vlonjatg.progressactivity.ProgressActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ted on 2015/8/27.
 */
public abstract class BaseLoadingFragment extends Fragment {
    private ProgressActivity mProgressActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mProgressActivity = (ProgressActivity) inflater.inflate(R.layout.fragment_base_loading_layout, container, false);
        mProgressActivity.addView(onCreateContentView(inflater, mProgressActivity, savedInstanceState));
        return mProgressActivity;
    }

    abstract View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected void showLoading() {
        mProgressActivity.showLoading();
    }

    protected void showContent(){
        mProgressActivity.showContent();
    }

    protected void showEmpty(Drawable emptyImageDrawable, String emptyTextTitle, String emptyTextContent,List<Integer> skipIds) {
        if(null == skipIds)skipIds = new ArrayList<>();
        mProgressActivity.showEmpty(emptyImageDrawable, emptyTextTitle, emptyTextContent,skipIds);
    }

    protected void showError(Drawable emptyImageDrawable, String emptyTextTitle, String emptyTextContent, String errorButtonText, View.OnClickListener onClickListener) {
        mProgressActivity.showError(emptyImageDrawable, emptyTextTitle, emptyTextContent, errorButtonText, onClickListener);
    }
}
