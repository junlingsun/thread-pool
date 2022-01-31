package com.junling.thread;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class ThreadPool {
    private  final Logger logger = LoggerFactory.getLogger(ThreadPool.class);

    private RejectPolicy<Runnable> rejectPolicy;
    private BlockQueue<Runnable> queue;
    private Set<Worker> workers = new HashSet<>();
    private Integer coreSize;
    private Long timeout;
    private TimeUnit timeUnit;

    public ThreadPool(Integer coreSize, Long timeout, TimeUnit timeUnit, Integer capacity, RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.rejectPolicy = rejectPolicy;
        queue = new BlockQueue<>(capacity);
    }

    public void execute(Runnable task) {
        if (workers.size() < coreSize) {
            synchronized (workers) {
                Worker worker = new Worker(task);
                logger.info("new worker {}", worker);
                workers.add(worker);
                worker.start();
            }
        }else {
            logger.info("add new task to queue {}", task);
            queue.tryPut(task, rejectPolicy);
        }
    }


    private class Worker extends Thread{
        private Runnable task;
        public Worker(Runnable task){
            this.task = task;
        }

        @Override
        public void run() {
            while (task != null || (task=queue.poll(timeout,timeUnit))!=null) {
                try{
                    logger.info("executing {}", task);
                    task.run();
                }catch (Exception e){
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }

            synchronized (workers) {
                logger.info("remove worker {}", this);
                workers.remove(this);
            }


        }
    }

}
