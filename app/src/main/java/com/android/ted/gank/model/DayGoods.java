package com.android.ted.gank.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DayGoods {

    @SerializedName("iOS")
    private ArrayList<Goods> iosGoods;
    @SerializedName("Android")
    private ArrayList<Goods> androidGoods;
    @SerializedName("瞎推荐")
    private ArrayList<Goods> recommend;
    @SerializedName("福利")
    private ArrayList<Goods> benefit;
    @SerializedName("休息视频")
    private ArrayList<Goods> restVideo;
    @SerializedName("拓展资源")
    private ArrayList<Goods> expandRes;

    public ArrayList<Goods> getIosGoods() {
        return iosGoods;
    }

    public void setIosGoods(ArrayList<Goods> iosGoods) {
        this.iosGoods = iosGoods;
    }

    public ArrayList<Goods> getAndroidGoods() {
        return androidGoods;
    }

    public void setAndroidGoods(ArrayList<Goods> androidGoods) {
        this.androidGoods = androidGoods;
    }

    public ArrayList<Goods> getRecommend() {
        return recommend;
    }

    public void setRecommend(ArrayList<Goods> recommend) {
        this.recommend = recommend;
    }

    public ArrayList<Goods> getBenefit() {
        return benefit;
    }

    public void setBenefit(ArrayList<Goods> benefit) {
        this.benefit = benefit;
    }

    public ArrayList<Goods> getRestVideo() {
        return restVideo;
    }

    public void setRestVideo(ArrayList<Goods> restVideo) {
        this.restVideo = restVideo;
    }

    public ArrayList<Goods> getExpandRes() {
        return expandRes;
    }

    public void setExpandRes(ArrayList<Goods> expandRes) {
        this.expandRes = expandRes;
    }
}
