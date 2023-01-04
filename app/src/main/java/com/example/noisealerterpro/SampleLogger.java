package com.example.noisealerterpro;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SampleLogger implements ISampleProcessor {
    private Context context;
    private FileOutputStream binaryWriter;

    SampleLogger(Context ctx)
    {
        context = ctx;
    }

    @Override
    public void init() {
        binaryWriter = openBinaryFile();
    }

    @Override
    public void close() {
        closeBinaryFile(binaryWriter);
    }

    @Override
    public void run(short[] samples, int size) {
        ByteBuffer bBuffer = ByteBuffer.allocate(samples.length * 2);
        bBuffer.order(ByteOrder.LITTLE_ENDIAN);
        bBuffer.asShortBuffer().put(samples);

        if (size > 0)
            writeToFile(binaryWriter, bBuffer.array(), size * 2);

        Log.i("LOGGER", "Logging");
    }

    private FileOutputStream openBinaryFile() {
        File binFile = new File(context.getFilesDir(), "samp" + System.currentTimeMillis() / 1000 + ".out");
        try {

            FileOutputStream binaryFile = new FileOutputStream(binFile);
            return binaryFile;
        }
        catch (IOException e) {
            Log.e("Exception", "File open failed: " + e.toString());
            return null;
        }
    }

    private void closeBinaryFile(FileOutputStream writer) {
        try {
            writer.close();
            writer = null;
        }
        catch (IOException e) {
            Log.e("Exception", "File close failed: " + e.toString());
        }
    }

    private void writeToFile(FileOutputStream writer, byte[] data, int size) {
        if (writer == null)
            return;
        try {
            writer.write(data, 0, size);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private OutputStreamWriter sampleWriter;

    private OutputStreamWriter  openFile(String filename) {
        try {

            OutputStreamWriter writer = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            return writer;
        }
        catch (IOException e) {
            Log.e("Exception", "File open failed: " + e.toString());
            return null;
        }
    }

    private void closeFile(OutputStreamWriter writer) {
        try {
            writer.close();
            writer = null;
        }
        catch (IOException e) {
            Log.e("Exception", "File close failed: " + e.toString());
        }
    }

    private void writeToFile(OutputStreamWriter writer, short[] data, int size) {
        if (writer == null)
            return;
        try {
            for (int i = 0; i < size; i ++) {
                writer.write(Integer.toString(data[i]) + "\n");
            }
//            Arrays.toString(data).substring(1, data);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
