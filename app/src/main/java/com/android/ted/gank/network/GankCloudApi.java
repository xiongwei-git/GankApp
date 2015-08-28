package com.android.ted.gank.network;


import com.android.ted.gank.GankApplication;
import com.android.ted.gank.config.Constants;
import com.android.ted.gank.model.DayGoodsResult;
import com.android.ted.gank.model.GoodsResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public class GankCloudApi {
    public static GankCloudApi instance;

    public static GankCloudApi getIns() {
        if (null == instance) {
            synchronized (GankCloudApi.class) {
                if (null == instance) {
                    instance = new GankCloudApi();
                }
            }
        }
        return instance;
    }

    /**每次加载条目*/
    public static final int LOAD_LIMIT = 20;
    /**加载起始页面*/
    public static final int LOAD_START = 1;

    public static final String ENDPOINT = Constants.GANK_SERVER_IP;

    private final GankCloudService mWebService;

    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();



    public GankCloudApi() {
        Cache cache;
        OkHttpClient okHttpClient = null;
        try {
            File cacheDir = new File(GankApplication.getContext().getCacheDir().getPath(), "gank_cache.json");
            cache = new Cache(cacheDir, 10 * 1024 * 1024);
            okHttpClient = new OkHttpClient();
            okHttpClient.setCache(cache);
        } catch (Exception e) {

        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(mRequestInterceptor)
                .build();
        mWebService = restAdapter.create(GankCloudService.class);
    }

    private RequestInterceptor mRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("Cache-Control", "public, max-age=" + 60 * 60 * 4);
            request.addHeader("Content-Type", "application/json");
        }
    };

    public interface GankCloudService {

        @GET("/data/Android/{limit}/{page}")
        Observable<GoodsResult> getAndroidGoods(
                @Path("limit") int limit,
                @Path("page") int page
        );

        @GET("/data/iOS/{limit}/{page}")
        Observable<GoodsResult> getIosGoods(
                @Path("limit") int limit,
                @Path("page") int page
        );

        @GET("/data/all/{limit}/{page}")
        Observable<GoodsResult> getAllGoods(
                @Path("limit") int limit,
                @Path("page") int page
        );

        @GET("/data/福利/{limit}/{page}")
        Observable<GoodsResult> getBenefitsGoods(
                @Path("limit") int limit,
                @Path("page") int page
        );

        @GET("/day/{year}/{month}/{day}")
        Observable<DayGoodsResult> getGoodsByDay(
                @Path("year") int year,
                @Path("month") int month,
                @Path("day") int day
        );
    }

    public Observable<GoodsResult> getCommonGoods(String type,int limit, int page) {
        if("Android".equalsIgnoreCase(type)){
            return mWebService.getAndroidGoods(limit, page);
        }
        if("IOS".equalsIgnoreCase(type)){
            return mWebService.getIosGoods(limit, page);
        }
        return mWebService.getAndroidGoods(limit, page);
    }

    public Observable<GoodsResult> getAndroidGoods(int limit, int page) {
        return mWebService.getAndroidGoods(limit, page);
    }

    public Observable<GoodsResult> getIosGoods(int limit, int page) {
        return mWebService.getIosGoods(limit, page);
    }

    public Observable<GoodsResult> getAllGoods(int limit, int page) {
        return mWebService.getAllGoods(limit, page);
    }

    public Observable<GoodsResult> getBenefitsGoods(int limit, int page) {
        return mWebService.getBenefitsGoods(limit, page);
    }

    public Observable<DayGoodsResult> getGoodsByDay(int year,int month,int day) {
        return mWebService.getGoodsByDay(year, month,day);
    }

}
