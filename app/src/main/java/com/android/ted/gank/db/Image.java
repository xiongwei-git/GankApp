package com.android.ted.gank.db;

import com.android.ted.gank.model.Goods;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Ted on 2015/8/26.
 */
public class Image extends RealmObject{
    /**补充数据*/
    private int width = 0;
    private int height = 0;
    private int position = 0;

    /**原始数据*/
    private String who;
    private String publishedAt;
    private String desc;
    private String type;
    private String url;
    private boolean used;
    private String objectId;
    private String createdAt;
    private String updatedAt;

    public static Image queryOrCreate(Realm realm,Goods goods){
        RealmResults<Image> results =  realm.where(Image.class).equalTo("objectId",goods.getObjectId()).findAll();
        if(results.size() > 0){
            return results.get(0);
        }
        return transformGoods(goods);
    }

    public static Image transformGoods(Goods goods){
        Image image = new Image();
        image.setWho(goods.getWho());
        image.setPublishedAt(goods.getPublishedAt());
        image.setDesc(goods.getDesc());
        image.setType(goods.getType());
        image.setUrl(goods.getUrl());
        image.setUsed(goods.isUsed());
        image.setObjectId(goods.getObjectId());
        image.setCreatedAt(goods.getCreatedAt());
        image.setUpdatedAt(goods.getUpdatedAt());
        return image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
