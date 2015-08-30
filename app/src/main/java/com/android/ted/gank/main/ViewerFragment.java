/*
 * Copyright 2015 XiNGRZ <chenxingyu92@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ted.gank.main;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.ted.gank.R;
import com.android.ted.gank.utils.PictUtil;
import com.android.ted.gank.view.TouchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class ViewerFragment extends Fragment{

    @Bind(R.id.image)
    TouchImageView image;

    private ViewerActivity activity;

    private String url;
    private boolean initialShown;
    private Bitmap mBitmap;

    public static ViewerFragment newFragment(String url, boolean initialShown) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putBoolean("initial_shown", initialShown);

        ViewerFragment fragment = new ViewerFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ViewerActivity){
            this.activity = (ViewerActivity)context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString("url");
        initialShown = getArguments().getBoolean("initial_shown", false);

        Logger.d("onResourceReady");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        //ViewCompat.setTransitionName(image, url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != mBitmap && !mBitmap.isRecycled()){
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Glide.with(this).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Logger.d("onResourceReady");
                if(null != resource){
                    image.setImageBitmap(resource);
                    //maybeStartPostponedEnterTransition();
                }else {
                    //getActivity().supportFinishAfterTransition();
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                Logger.d("onLoadFailed");
//                maybeStartPostponedEnterTransition();
//                getActivity().supportFinishAfterTransition();
            }
        });
    }

//    private void maybeStartPostponedEnterTransition() {
//        if (initialShown) {
//            activity.supportStartPostponedEnterTransition();
//        }
//    }

    @OnClick(R.id.image)
    @SuppressWarnings("unused")
    void toggleToolbar() {
        activity.toggleToolbar();
    }

    @OnLongClick(R.id.image)
    @SuppressWarnings("unused")
    boolean setImageToWallpaper(){
        if(!PictUtil.hasSDCard()){
            Toast.makeText(getActivity(),"不支持下载文件",Toast.LENGTH_SHORT).show();
            return false;
        }
        Glide.with(this).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mBitmap = resource;
                saveImgFileToLocal();
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                mBitmap = null;
                Toast.makeText(getActivity(),"下载图片失败，请重试",Toast.LENGTH_SHORT).show();
            }
        });
        return false;
    }

    private void saveImgFileToLocal(){
        if(null != mBitmap){
            //create a temporary directory within the cache folder
            File dir = new File(getActivity().getCacheDir() + "/images");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //create the file
            File file = new File(dir, PictUtil.getImageFileName(url));
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                PictUtil.saveToFile(file,mBitmap);
            }catch (IOException e){
                Logger.e(e,"下载图片失败");
                Toast.makeText(getActivity(),"下载图片失败，请重试",Toast.LENGTH_SHORT).show();
            }finally {
                checkFileAndSetWallPaper(file);
            }
        }else {
            Toast.makeText(getActivity(),"下载图片失败，请重试",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFileAndSetWallPaper(File file){
        if(null != file && file.exists()){
            //get the contentUri for this file and start the intent
            Uri contentUri = FileProvider.getUriForFile(getActivity(), "com.android.ted.gank.fileprovider", file);
            //get crop intent
            Intent intent = WallpaperManager.getInstance(getActivity()).getCropAndSetWallpaperIntent(contentUri);
            //start activity for result so we can animate if we finish
            getActivity().startActivityForResult(intent, ViewerActivity.REQUEST_CODE_SET_WALLPAPER);
        }
    }

//    View getSharedElement() {
//        return image;
//    }

}
