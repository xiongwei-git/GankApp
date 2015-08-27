package com.android.ted.gank.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ted.gank.R;
import com.android.ted.gank.data.ImageGoodsCache;
import com.android.ted.gank.db.Image;
import com.android.ted.gank.manager.CollectManager;
import com.android.ted.gank.model.Goods;
import com.android.ted.gank.utils.Utils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by froger_mcs on 05.11.14.
 */
public class GoodsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private static final int ANIMATED_ITEMS_COUNT = 3;

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animateItems = false;

    private ArrayList<Goods> goodsItemData;

    public GoodsItemAdapter(Context context) {
        this.context = context;
        goodsItemData = new ArrayList<>();
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if(viewId == R.id.img_like_goods){

        }
    }

    private View.OnClickListener mItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer position = (Integer)view.getTag();
            Goods goods = goodsItemData.get(position.intValue());
            Intent intent= new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(goods.getUrl());
            intent.setData(content_url);
            view.getContext().startActivity(intent);
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.goods_item_layout, parent, false);
        final CellGoodsViewHolder cellGoodsViewHolder = new CellGoodsViewHolder(view);
        cellGoodsViewHolder.imgLikeGoods.setOnClickListener(this);
        cellGoodsViewHolder.rootView.setOnClickListener(mItemOnClickListener);
        return cellGoodsViewHolder;
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
        Goods goods = goodsItemData.get(position);
        Image image = ImageGoodsCache.getIns().getImgGoodsRandom(position);
        boolean hasImg = null != image;
        holder.txtGoodsTitle.setText("#"+goods.getDesc());
        holder.txtImgAuthor.setText(hasImg?"图："+image.getWho():"");
        holder.txtGoodsAuthor.setText(getGoodsAuthorInfo(goods));
        loadGoodsImage(holder, image);
        updateHeartButton(holder, goods, false);

        holder.imgLikeGoods.setTag(holder);
        holder.rootView.setTag(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return goodsItemData.size();
    }

    private String getGoodsAuthorInfo(Goods goods){
        StringBuilder builder = new StringBuilder();
        Date date = Utils.formatDateFromStr(goods.getPublishedAt());
        String dateStr = Utils.getFormatDateStr(date);
        builder.append(goods.getWho()).append(TextUtils.isEmpty(dateStr)?"":"@"+dateStr);
        return builder.toString();
    }

    private void loadGoodsImage(final CellGoodsViewHolder holder,Image imgGoods){
        if(null == imgGoods || TextUtils.isEmpty(imgGoods.getUrl())){
            Glide.with(context)
                    .load(R.drawable.item_default_img)
                    .centerCrop()
                    .into(holder.imgGoodsImageBg);
        }else {
            Glide.with(context)
                    .load(imgGoods.getUrl())
                    .centerCrop()
                    .into(holder.imgGoodsImageBg);
        }
    }


    private void updateHeartButton(final CellGoodsViewHolder holder,Goods goods, boolean animated) {
        if (animated) {
            if (!CollectManager.getIns().isCollect(goods)) {
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.imgLikeGoods, "rotation", 0f, 360f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.imgLikeGoods, "scaleX", 0.2f, 1f);
                bounceAnimX.setDuration(300);
                bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.imgLikeGoods, "scaleY", 0.2f, 1f);
                bounceAnimY.setDuration(300);
                bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
                bounceAnimY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        holder.imgLikeGoods.setImageResource(R.drawable.ic_heart_red);
                    }
                });

                animatorSet.play(rotationAnim);
                animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //resetLikeAnimationState(holder);
                    }
                });
                animatorSet.start();
            }else {

            }
        } else {
            if (CollectManager.getIns().isCollect(goods)) {
                holder.imgLikeGoods.setImageResource(R.drawable.ic_heart_red);
            } else {
                holder.imgLikeGoods.setImageResource(R.drawable.ic_heart_outline_grey);
            }
        }
    }



    private void animatePhotoLike(final CellGoodsViewHolder holder) {
//        if (!likeAnimations.containsKey(holder)) {
//            holder.vBgLike.setVisibility(View.VISIBLE);
//            holder.ivLike.setVisibility(View.VISIBLE);
//
//            holder.vBgLike.setScaleY(0.1f);
//            holder.vBgLike.setScaleX(0.1f);
//            holder.vBgLike.setAlpha(1f);
//            holder.ivLike.setScaleY(0.1f);
//            holder.ivLike.setScaleX(0.1f);
//
//            AnimatorSet animatorSet = new AnimatorSet();
//            likeAnimations.put(holder, animatorSet);
//
//            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
//            bgScaleYAnim.setDuration(200);
//            bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
//            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
//            bgScaleXAnim.setDuration(200);
//            bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
//            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
//            bgAlphaAnim.setDuration(200);
//            bgAlphaAnim.setStartDelay(150);
//            bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
//
//            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
//            imgScaleUpYAnim.setDuration(300);
//            imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
//            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
//            imgScaleUpXAnim.setDuration(300);
//            imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
//
//            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
//            imgScaleDownYAnim.setDuration(300);
//            imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
//            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
//            imgScaleDownXAnim.setDuration(300);
//            imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
//
//            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
//            animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);
//
//            animatorSet.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    resetLikeAnimationState(holder);
//                }
//            });
//            animatorSet.start();
//        }
    }

    public void updateItems(ArrayList<Goods> goods,boolean animated) {
        goodsItemData.clear();
        goodsItemData.addAll(goods);
        animateItems = animated;
        notifyDataSetChanged();
    }


    public static class CellGoodsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.img_goods_img)
        ImageView imgGoodsImageBg;
        @Bind(R.id.txt_img_author)
        TextView txtImgAuthor;
        @Bind(R.id.txt_goods_title)
        TextView txtGoodsTitle;
        @Bind(R.id.img_like_goods)
        ImageView imgLikeGoods;
        @Bind(R.id.txt_goods_author)
        TextView txtGoodsAuthor;

        public final View rootView;

        public CellGoodsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            rootView = view;
        }

    }
}
