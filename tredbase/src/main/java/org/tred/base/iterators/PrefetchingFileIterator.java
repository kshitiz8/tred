package org.tred.base.iterators;

import org.tred.base.concurrent.SemaphoreBlockingQueue;
import org.tred.base.concurrent.WaitNotifyBlockingQueue;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PrefetchingFileIterator implements Iterator<String> {
    final File file;
    final InputStream in;
    final int prefetchSize;
    final BlockingQueue<String> blockingQueue;

    private volatile  boolean done = false;
    final Object lock = new Object();

    public PrefetchingFileIterator(File file,  int prefetchSize) throws FileNotFoundException {
        this(file, new ArrayBlockingQueue<String>(prefetchSize));
    }

    public PrefetchingFileIterator(File file,  BlockingQueue<String> blockingQueue) throws FileNotFoundException {
        this.file = file;
        in = new FileInputStream(file);
        this.prefetchSize = blockingQueue.remainingCapacity();
        this.blockingQueue = blockingQueue;

        Thread t = new Thread(this::produce,"producer");
        t.setDaemon(true);
        t.start();
    }

    private void produce(){
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = br.readLine()) != null){
//                Thread.sleep(10);
//                System.out.println(String.format("%s: %s",Thread.currentThread().getName(),line));
                blockingQueue.put(line);
            }
            synchronized (lock) {
                done = true;
                lock.notifyAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {

        if(blockingQueue.size() > 0) return true;
        if(done) return false;
        synchronized (lock) {
            while (!done && blockingQueue.size() == 0) {
                try {
                    lock.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return blockingQueue.size() > 0;
        }


    }

    @Override
    public String next() {
        try {
            while(hasNext()) {
                String ret = blockingQueue.take();
                if(ret == null){continue;}
                return ret;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        return null;
    }
}
