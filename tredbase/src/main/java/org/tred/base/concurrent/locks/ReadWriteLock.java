package org.tred.base.concurrent.locks;

/**
 * Imagine you have an application where you have multiple readers and multiple writers. You are asked to design a lock
 * which lets multiple readers read at the same time, but only one writer write at a time.
 */
class Demonstration {

    public static void main(String args[]) throws Exception {

        final ReadWriteLock rwl = new ReadWriteLock();

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    System.out.println("Attempting to acquire write lock in t1: " + System.currentTimeMillis());
                    rwl.acquireWriteLock();
                    System.out.println("write lock acquired t1: " + +System.currentTimeMillis());

//                    // Simulates write lock being held indefinitely
//                    for (int i=0; i<10; i++) {
                        Thread.sleep(500);
                        rwl.releaseWriteLock();
//                    }

                } catch (InterruptedException ie) {

                }
            }
        });

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    System.out.println("Attempting to acquire write lock in t2: " + System.currentTimeMillis());
                    rwl.acquireWriteLock();
                    System.out.println("write lock acquired t2: " + System.currentTimeMillis());
                    Thread.sleep(500);
                    rwl.releaseReadLock();

                } catch (InterruptedException ie) {

                }
            }
        });

        Thread tReader1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    rwl.acquireReadLock();
                    System.out.println("Read lock acquired: " + System.currentTimeMillis());
                    Thread.sleep(500);
                    rwl.releaseReadLock();
                    System.out.println("Read lock released: " + System.currentTimeMillis());
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread tReader2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    rwl.acquireReadLock();
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Read lock about to release: " + System.currentTimeMillis());
                rwl.releaseReadLock();
                System.out.println("Read lock released: " + System.currentTimeMillis());
            }
        });

        tReader1.start();
        t1.start();
        Thread.sleep(3000);
        tReader2.start();
        Thread.sleep(1000);
        t2.start();
        tReader1.join();
        tReader2.join();
        t2.join();
    }
}

public class ReadWriteLock {
    boolean writing =false;
    int reads=0;
    public synchronized void acquireReadLock() throws InterruptedException{
        while(writing){
            wait();
        }
        reads++;
    }
    public synchronized void releaseReadLock(){
        reads--;
        notifyAll();
    }


    public synchronized void acquireWriteLock() throws InterruptedException{
        while(writing || reads>0){
            wait();
        }
        writing=true;
    }


    public synchronized void releaseWriteLock(){
        writing=false;
        notifyAll();
    }

}
