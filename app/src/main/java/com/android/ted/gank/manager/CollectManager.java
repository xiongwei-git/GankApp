package com.android.ted.gank.manager;

import com.android.ted.gank.model.Goods;

/**
 * Created by Ted on 2015/8/25.
 */
public class CollectManager {
    private static CollectManager instance;

    public static CollectManager getIns() {
        if (null == instance) {
            synchronized (CollectManager.class) {
                if (null == instance) {
                    instance = new CollectManager();
                }
            }
        }
        return instance;
    }

    public boolean isCollect(Goods goods){
        return false;
    }
}
