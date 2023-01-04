package com.example.noisealerterpro;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public abstract class BitmapPlotter {
    int width, height;
    protected int plotHeight;
    int curX;
    protected Bitmap bmp;
    private boolean scrolling;

    BitmapPlotter(int w, int h)
    {
        init(w, h);
    }

    void init(int w, int h) {
        width = w * 2;
        height = h;
        plotHeight = h;
        curX = 0;
        scrolling = false;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bmp = Bitmap.createBitmap(width, height, conf);
        bmp.eraseColor(Color.argb(0, 0, 0, 0));
    }

    abstract void updateBitmap();

    void draw(int left, int top, Canvas canvas, Paint paint) {
        int plotBottom = top + plotHeight;
        Rect dst = new Rect(left, top, width/2 + left, plotBottom);
        if (curX < width / 2) {
            if (true) {
                Rect src = new Rect(0, 0, curX, height);
                dst = new Rect(left + width/2-curX, top, width/2 + left, plotBottom);
                canvas.drawBitmap(bmp, src, dst, null);
                src = new Rect(curX+width/2, 0, width, height);
                dst = new Rect(left, top, left + width/2-curX, plotBottom);
                canvas.drawBitmap(bmp, src, dst, null);
            } else {
                Rect src = new Rect(0, 0, width / 2, height);
                canvas.drawBitmap(bmp, src, dst, null);
            }
        }
        else {
            Rect src = new Rect(curX - width / 2, 0, curX, height);
            canvas.drawBitmap(bmp, src, dst, null);
            scrolling = true;
        }
        Utils.drawRecessedFrame(left, top, width/2 + left, plotBottom, canvas, paint);
    }

    void drawStatic(int left, int top, Canvas canvas, Paint paint) {
        int plotBottom = top + plotHeight;
        Rect src = new Rect(0, 0, width/2, height);
        Rect dst = new Rect(left, top, width/2 + left, plotBottom);
        canvas.drawBitmap(bmp, src, dst, null);
        Utils.drawRecessedFrame(left, top, width/2 + left, plotBottom, canvas, paint);
    }
}
