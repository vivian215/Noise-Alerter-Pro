package com.example.noisealerterpro;

import android.graphics.Color;

public class MFCCPlotter extends BitmapPlotter {
    MFCCPlotter(int w, int h) {
        super(w, h);
        plotHeight = 256;
    }

    void updateBitmap() {
        double[][] spectrum = AppData.get().mfcc;
        int w = spectrum[0].length - 1;

        for (int x = 0; x < w; x ++) {
            if (x+curX >= width)
                break;
            for (int y = 0; y < spectrum.length; y++) {
                float red = (float) (spectrum[spectrum.length-y-1][x] + 150)/300;
                if (red < 0.0f)
                    red = 0.0f;
                if (red > 1.0f)
                    red = 1.0f;
                bmp.setPixel(x + curX, y, Color.rgb(red, 0.8f*(1.0f-red), 1.0f*(1.0f-red)));
            }
        }
        curX += w;
        if (curX >= width) {
            curX = 0;
        }
    }
}
