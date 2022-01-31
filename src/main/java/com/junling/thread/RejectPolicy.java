package com.junling.thread;

public interface RejectPolicy<T> {

    void reject(BlockQueue<T> queue, T task);
}
