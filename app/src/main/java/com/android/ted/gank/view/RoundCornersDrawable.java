package com.android.ted.gank.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Image with rounded corners
 * <p/>
 * You can find the original source here:
 * http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
/**
 * Created by Ted on 2015/8/24.
 */
public class RoundCornersDrawable extends Drawable {

    private final float mCornerRadius;
    private final RectF mRect = new RectF();
    //private final RectF mRectBottomR = new RectF();
    //private final RectF mRectBottomL = new RectF();
    private final BitmapShader mBitmapShader;
    private final Paint mPaint;
    private final int mMargin;

    public RoundCornersDrawable(Bitmap bitmap, float cornerRadius, int margin) {
        mCornerRadius = cornerRadius;

        mBitmapShader = new BitmapShader(bitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(mBitmapShader);

        mMargin = margin;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRect.set(mMargin, mMargin, bounds.width() - mMargin, bounds.height() - mMargin);
        //mRectBottomR.set( (bounds.width() -mMargin) / 2, (bounds.height() -mMargin)/ 2,bounds.width() - mMargin, bounds.height() - mMargin);
        // mRectBottomL.set( 0,  (bounds.height() -mMargin) / 2, (bounds.width() -mMargin)/ 2, bounds.height() - mMargin);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
        //canvas.drawRect(mRectBottomR, mPaint); //only bottom-right corner not rounded
        //canvas.drawRect(mRectBottomL, mPaint); //only bottom-left corner not rounded

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }


}