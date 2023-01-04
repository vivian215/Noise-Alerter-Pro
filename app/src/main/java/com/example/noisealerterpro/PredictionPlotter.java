package com.example.noisealerterpro;

import android.graphics.Color;
import android.util.Log;

public class PredictionPlotter extends BitmapPlotter {

    PredictionPlotter(int w, int h) {
        super(w, h);
        plotHeight = h * 2;
    }

    void updateBitmap() {
        int w = 20;

        for (int x = 2; x < w-2; x ++) { //20
            if (x+curX >= width)
                break;
            int numTypes = 7;
            int heightPerType = height / numTypes;
            if (AppData.get().detectCount > 1) {
                float[] result = AppData.get().predictResult;
                for (int i = 0; i < numTypes; i ++) { // seven sound types
                    for (int y = heightPerType * i; y < heightPerType * (i+1); y++) { //128
                        int nType = i;
                        if (y - heightPerType * i >= (int)((1.0-result[nType]) * heightPerType)) {
                            int green = (int)(result[nType] * 255.0);
                            bmp.setPixel(x + curX, y, Color.rgb(255 - green, 0 + green, 0));
                        }
                        else {
                            bmp.setPixel(x + curX, y, Color.argb(255, 183, 201, 226));
                        }
                    }
                }
            }
            else {
                for (int y = 0; y < height; y++) { //128
                    bmp.setPixel(x + curX, y, Color.argb(255, 183, 201, 226));
                }
            }
        }
        curX += w;
        if (curX >= width) {
            curX = 0;
        }
    }
}
