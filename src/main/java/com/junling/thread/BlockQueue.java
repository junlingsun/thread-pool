package com.junling.thread;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockQueue<T> {

    private Integer capacity;
    private Deque<T> queue = new ArrayDeque<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition fullWaitingSet = lock.newCondition();
    private Condition emptyWaitingSet = lock.newCondition();

    public BlockQueue(Integer capacity) {
        this.capacity = capacity;
        queue = new ArrayDeque<>(capacity);
    }

    /**
     * block while queue is empty
      * @return
     */
    public T take(){
        lock.lock();

        try{
            while (queue.isEmpty()) {
                emptyWaitingSet.await();
            }
            T element = queue.pollFirst();
            fullWaitingSet.signal();
            return element;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public T poll(Long time, TimeUnit timeUnit) {
        lock.lock();
        long timeout = timeUnit.toNanos(time);
        try{
            while(queue.isEmpty()) {
                timeout = emptyWaitingSet.awaitNanos(timeout);
                if (timeout <= 0) return null;
            }

            fullWaitingSet.signal();
            return queue.pollFirst();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return null;
    }

    /**
     * block while queue is full
     * @param element
     */
    public void put(T element) {
        lock.lock();

        try{
            while(queue.size()==capacity) {
                fullWaitingSet.await();
            }

            queue.addLast(element);
            emptyWaitingSet.signal();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(T element, Long timeout, TimeUnit timeUnit) {
        lock.lock();
        try{
            long nanos = timeUnit.toNanos(timeout);
            while (queue.size() == capacity) {
                nanos = fullWaitingSet.awaitNanos(nanos);
                if (nanos < 0) return false;
            }

            queue.addLast(element);
            emptyWaitingSet.signal();
            return true;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return true;

    }

    /**
     * return the number of elements in the queue
     * @return
     */
    public Integer size(){
        lock.lock();
        try{
            return queue.size();
        }finally {
            lock.unlock();
        }
    }


    public void tryPut(T task, RejectPolicy<T> rejectPolicy) {
        lock.lock();
        try{
            if (queue.size() == capacity) {
                rejectPolicy.reject(this, task);

            }else {
                queue.addLast(task);
                emptyWaitingSet.signal();
            }
        }finally {
            lock.unlock();
        }
    }
}
