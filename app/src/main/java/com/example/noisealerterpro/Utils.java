package com.example.noisealerterpro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {
    Context context;

    Utils(Context context) {
        this.context = context;
    }

    public FileOutputStream openBinaryFile(File dir, String prefix) {
        File binFile = new File(dir, prefix + System.currentTimeMillis() / 1000 + ".out");
        try {

            FileOutputStream binaryFile = new FileOutputStream(binFile);
            return binaryFile;
        }
        catch (IOException e) {
            Log.e("Exception", "File open failed: " + e.toString());
            return null;
        }
    }

    public void closeBinaryFile(FileOutputStream writer) {
        try {
            writer.close();
            writer = null;
        }
        catch (IOException e) {
            Log.e("Exception", "File close failed: " + e.toString());
        }
    }

    public void writeToFile(FileOutputStream writer, byte[] data, int size) {
        if (writer == null)
            return;
        try {
            writer.write(data, 0, size);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void writeToFile(FileOutputStream writer, float[] data, int size) {
        ByteBuffer bBuffer = ByteBuffer.allocate(data.length * 4);
        bBuffer.order(ByteOrder.LITTLE_ENDIAN);
        bBuffer.asFloatBuffer().put(data);
        writeToFile(writer, bBuffer.array(), size * 4);
    }

    public void writeToFile(FileOutputStream writer, short[] data, int size) {
        ByteBuffer bBuffer = ByteBuffer.allocate(data.length * 2);
        bBuffer.order(ByteOrder.LITTLE_ENDIAN);
        bBuffer.asShortBuffer().put(data);
        writeToFile(writer, bBuffer.array(), size * 2);
    }

    static void drawRecessedFrame(int left, int top, int right, int bottom, Canvas canvas, Paint paint)
    {
        int strokeWidth = 10;
        left -= strokeWidth/2;
        top -= strokeWidth/2;
        bottom += strokeWidth/2;
        right += strokeWidth/2;
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.rgb(163, 181, 206));
        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        paint.setColor(Color.rgb(203, 221, 246));
        canvas.drawLine(left, bottom, right, bottom, paint);
        canvas.drawLine(right, top, right, bottom, paint);
    }

    static void drawRaisedFrame(int left, int top, int right, int bottom, Canvas canvas, Paint paint)
    {
        int strokeWidth = 10;
        left -= strokeWidth/2;
        top -= strokeWidth/2;
        bottom += strokeWidth/2;
        right += strokeWidth/2;
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.rgb(203, 221, 246));
        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        paint.setColor(Color.rgb(163, 181, 206));
        canvas.drawLine(left, bottom, right, bottom, paint);
        canvas.drawLine(right, top, right, bottom, paint);
    }
}
