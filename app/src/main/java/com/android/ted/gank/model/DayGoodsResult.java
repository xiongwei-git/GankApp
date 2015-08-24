package com.android.ted.gank.model;

import java.util.ArrayList;

/**
 * Created by Ted on 2015/8/24.
 */
public class DayGoodsResult extends BaseResult{
    private DayGoods results;
    private ArrayList<String> category;

    public DayGoods getResults() {
        return results;
    }

    public void setResults(DayGoods results) {
        this.results = results;
    }

    public ArrayList<String> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<String> category) {
        this.category = category;
    }
}
