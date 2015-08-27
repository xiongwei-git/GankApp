package com.android.ted.gank.model;

/**
 * Created by Ted on 2015/8/23.
 * 干货实体类
 */
public class Goods{

//            "who":"Jason",
//            "publishedAt":"2015-08-21T04:09:13.777Z",
//            "desc":"着色的加载视图库",
//            "type":"Android",
//            "url":"https://github.com/recruit-lifestyle/ColoringLoading",
//            "used":true,
//            "objectId":"55d5fd6b00b0af5bde3a9f82",
//            "createdAt":"2015-08-20T16:16:43.941Z",
//            "updatedAt":"2015-08-21T04:09:14.358Z"
    private String who;
    private String publishedAt;
    private String desc;
    private String type;
    private String url;
    private boolean used;
    private String objectId;
    private String createdAt;
    private String updatedAt;

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
