package org.tred.base.concurrent;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {

    private final int capacity;
    private int count = 0;
    private int toRelease = 0;
    private Lock lock = new ReentrantLock();
    private Condition full = lock.newCondition();
    private Condition removedAll = lock.newCondition();

    public Barrier(int capacity) {
        this.capacity = capacity;
    }

    public void await() throws InterruptedException{
        lock.lock();

        while(count == capacity) removedAll.await();

        count++;
        if(count == capacity){
            toRelease = capacity;
            full.signalAll();
        }else{
            while(count<capacity && toRelease==0) full.await();
        }

        toRelease--;
        if(toRelease == 0){
            count =0;
            removedAll.signalAll();
        }
        lock.unlock();
    }


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Barrier barrier = new Barrier(3);
        for(int i=0;i<12;i++){
            int id = i;
            executorService.execute(()->{
                try {
                    System.out.println(id + "waiting");
                    barrier.await();
                    System.out.println(id + "released");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Thread.sleep(100);
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }
}
