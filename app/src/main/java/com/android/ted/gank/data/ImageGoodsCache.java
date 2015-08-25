package com.android.ted.gank.data;

import com.android.ted.gank.model.Goods;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ted on 2015/8/24.
 * 图片缓存
 */
public class ImageGoodsCache {
    private static ImageGoodsCache instance;

    private ArrayList<Goods> mGankImageList;

    public ImageGoodsCache(){
        mGankImageList = new ArrayList<>();
    }

    public static ImageGoodsCache getIns() {
        if (null == instance) {
            synchronized (ImageGoodsCache.class) {
                if (null == instance) {
                    instance = new ImageGoodsCache();
                }
            }
        }
        return instance;
    }

    /***
     * 新增图片对象
     * @param image
     */
    public void addImageGoods(Goods image){
        mGankImageList.add(image);
    }

    public void addAllImageGoods(ArrayList<Goods> list){
        if(null != list && list.size() > 0){
            mGankImageList.clear();
            mGankImageList.addAll(list);
        }
    }

    public ArrayList<Goods> getGankImageList() {
        return mGankImageList;
    }

    public Goods getImgGoodsRandom(int randomIndex){
        int size = getGankImageList().size();
        if(size == 0)return null;
        Random random = new Random();
        int randomInt = random.nextInt(size);
        if(randomInt + randomIndex >= size){
            return getGankImageList().get(randomInt);
        }
        return getGankImageList().get(randomInt + randomIndex);
    }
}
