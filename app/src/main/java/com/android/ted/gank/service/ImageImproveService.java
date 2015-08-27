
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

package com.android.ted.gank.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.android.ted.gank.db.Image;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 完善图片信息的服务
 *
 * @author Ted
 */
public class ImageImproveService extends IntentService{

    public static final String ACTION_UPDATE_RESULT = "com.android.ted.UPDATE_RESULT";

    public static final String EXTRA_CHANGE = "change_data";
    public static final String EXTRA_ACTION = "action_name";

    public static final String PERMISSION_ACCESS_UPDATE_RESULT = "com.android.ted.ACCESS_UPDATE_RESULT";

    public static final String ACTION_IMPROVE_IMAGE = "com.android.ted.IMPROVE_IMAGE";


    private static final String TAG = "ImageImproveService";

    private final OkHttpClient client = new OkHttpClient();

    public ImageImproveService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getInstance(this);

        /**查询所有尺寸为0的图片的数目*/
        int count = realm.where(Image.class).equalTo("width",0)
                .findAllSorted("position", RealmResults.SORT_ORDER_DESCENDING).size();
        Logger.d(count + " image need improve");

        if (count == 0) {
            Logger.d("no new image, fresh fetch");
        } else if (ACTION_IMPROVE_IMAGE.equals(intent.getAction())) {
            improveImageInfo(count,realm);
        }

        realm.close();

        Logger.d("finished improve num:" + count);

        Intent broadcast = new Intent(ACTION_UPDATE_RESULT);
        broadcast.putExtra(EXTRA_CHANGE, count);
        broadcast.putExtra(EXTRA_ACTION, intent.getAction());
        sendBroadcast(broadcast, PERMISSION_ACCESS_UPDATE_RESULT);
    }

    private void improveImageInfo(int count,Realm realm){
        for(int i = 0;i < count;i++){
            Image image = Image.queryFirstZeroImg(realm);
            if(null != image)
                saveToDb(realm,image);
        }
    }


    /**
     * 预解码图片并将抓到的图片尺寸保存至数据库
     *
     * @param realm   Realm 实例
     * @param image 图片
     * @return 是否保存成功
     */
    private boolean saveToDb(Realm realm, Image image) {
        realm.beginTransaction();
        try {
            Point size = new Point();
            loadImageForSize(image.getUrl(),size);
            image.setHeight(size.y);
            image.setWidth(size.x);
            realm.copyToRealmOrUpdate(image);
        } catch (IOException e) {
            Logger.d("Failed to fetch image");
            realm.cancelTransaction();
            return false;
        }
        realm.commitTransaction();
        return true;
    }

    /***
     * 加载图片内容计算图片大小
     * @param url
     * @param measured
     * @throws IOException
     */
    public void loadImageForSize(String url, Point measured) throws IOException {
        Response response = client.newCall(new Request.Builder().url(url).build()).execute();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(response.body().byteStream(), null, options);
        measured.x = options.outWidth;
        measured.y = options.outHeight;
    }

}
