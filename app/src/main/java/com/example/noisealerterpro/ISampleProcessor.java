package com.example.noisealerterpro;

public interface ISampleProcessor {
    void init();
    void close();
    void run(short[] samples, int size);
}
