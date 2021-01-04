package org.tred.base.concurrent.semaphore;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * Java does provide its own implementation of Semaphore, however, Java's semaphore is initialized with an initial
 * number of permits, rather than the maximum possible permits and the developer is expected to take care of always
 * releasing the intended number of maximum permits.
 *
 * Briefly, a semaphore is a construct that allows some threads to access a fixed set of resources in parallel. Always
 * think of a semaphore as having a fixed number of permits to give out. Once all the permits are given out, requesting
 * threads, need to wait for a permit to be returned before proceeding forward.
 *
 * Your task is to implement a semaphore which takes in its constructor the maximum number of permits allowed and is
 * also initialized with the same number of permits.

 */
public class BoundedSemaphore {
    int availablePermits;
    final int maxPermits;

    final Lock lock;
    Condition permitReleased;
    Condition permitAcquired;

    public BoundedSemaphore(int maxPermits, boolean fair){
        this.maxPermits = maxPermits;
        this.availablePermits = maxPermits;
        lock = new ReentrantLock(fair);
        permitReleased = lock.newCondition();
        permitAcquired = lock.newCondition();
    }

    public void acquire() throws InterruptedException {
        lock.lock();
        while(availablePermits==0){
            permitReleased.await();
        }
        availablePermits--;
        permitAcquired.signalAll();
        lock.unlock();
    }

    public void release() throws InterruptedException {
        lock.lock();
        while (availablePermits == maxPermits){
            permitAcquired.await();
        }
        availablePermits++;
        permitReleased.signalAll();
        lock.unlock();
    }

    public int getAvailablePermits(){
        try{
            lock.lock();
            return availablePermits;
        }finally {
            lock.unlock();
        }
    }


    public static void main( String args[] ) throws InterruptedException {

        final BoundedSemaphore bs = new BoundedSemaphore(1, false);

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < 5; i++) {
                        bs.acquire();
                        System.out.println("Ping " + i);
                    }
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    try {
                        bs.release();
                        System.out.println("Pong " + i);
                    } catch (InterruptedException ie) {

                    }
                }
            }
        });

        t2.start();
        t1.start();
        t1.join();
        t2.join();
    }
}
