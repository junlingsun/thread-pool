package com.junling.thread;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class test {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(2, 1000L, TimeUnit.MILLISECONDS, 10, (q, t)->{
            //option1: keep waiting
            //q.put(t);

            //option2: waiting w/ timeout
            q.offer(t,1000L, TimeUnit.MILLISECONDS);

            //option3: throw additional task
            //do nothing.

            //option4: throw exceptions.
            //throw new RuntimeException("queue is full");

            //option5: main thread to execute the task
            //t.run();
        });

        for (int i = 0; i < 5; i++) {
            int j = i;
            threadPool.execute(()->{
                 System.out.println(j);
             });
        }


    }
}
