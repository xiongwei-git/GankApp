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

package com.android.ted.gank.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.android.ted.gank.R;
import com.android.ted.gank.db.Image;
import com.android.ted.gank.utils.Utils;
import com.android.ted.gank.view.RadioImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by froger_mcs on 05.11.14.
 */
public abstract class BenefitGoodsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private final LayoutInflater inflater;

    private static final int ANIMATED_ITEMS_COUNT = 1;

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animateItems = false;

    private ArrayList<Image> imageDataList;

    public BenefitGoodsItemAdapter(Context context) {
        this.context = context;
        imageDataList = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if(viewId == R.id.img_like_goods){

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CellGoodsViewHolder(R.layout.benefit_goods_item_layout, parent);
    }

    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(context));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        final CellGoodsViewHolder holder = (CellGoodsViewHolder) viewHolder;
        bindGoodsItem(position, holder);
    }

    private void bindGoodsItem(int position, CellGoodsViewHolder holder) {
        Image image = imageDataList.get(position);
        holder.imageView.setOriginalSize(image.getWidth(), image.getHeight());
        loadGoodsImage(holder, image);
        ViewCompat.setTransitionName(holder.imageView, image.getUrl());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return imageDataList.size();
    }

    private void loadGoodsImage(final CellGoodsViewHolder holder, Image imgGoods) {
        if (null == imgGoods || TextUtils.isEmpty(imgGoods.getUrl())) {
            Glide.with(context)
                    .load(R.drawable.item_default_img)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            Glide.with(context)
                    .load(imgGoods.getUrl())
                    .centerCrop()
                    .into(holder.imageView);
        }
    }

    public void updateItems(ArrayList<Image> images,boolean animated) {
        imageDataList.clear();
        imageDataList.addAll(images);
        animateItems = animated;
        notifyDataSetChanged();
    }

    protected abstract void onItemClick(View v, int position);


    public class CellGoodsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.image)
        public RadioImageView imageView;

        public CellGoodsViewHolder(@LayoutRes int resource, ViewGroup parent) {
            super(inflater.inflate(resource, parent, false));
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(v, getAdapterPosition());
                }
            });
        }

    }
}
