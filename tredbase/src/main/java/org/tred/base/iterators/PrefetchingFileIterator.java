package org.tred.base.iterators;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.tred.base.concurrent.WaitNotifyBlockingQueue;

public class PrefetchingFileIterator implements Iterator<String> {
    final File file;
    final InputStream in;
    final int prefetchSize;
    final BlockingQueue<String> blockingQueue;

    private volatile  boolean done = false;

    public PrefetchingFileIterator(File file,  int prefetchSize) throws FileNotFoundException {
        this.file = file;
        in = new FileInputStream(file);
        this.prefetchSize = prefetchSize;
        this.blockingQueue = new WaitNotifyBlockingQueue<String>(prefetchSize);

        Thread t = new Thread(this::produce,"producer");
        t.setDaemon(true);
        t.start();
    }

    private void produce(){
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = br.readLine()) != null){
//                System.out.println(String.format("%s: %s",Thread.currentThread().getName(),line));
                blockingQueue.put(line);
            }
            done=true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        return blockingQueue.size()>0 || !done;
//        return false;
    }

    @Override
    public String next() {
        try {
            while(hasNext()) {
                String ret = blockingQueue.poll(10, TimeUnit.MILLISECONDS);
                if(ret == null){continue;}
                return ret;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        return null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        long st = System.currentTimeMillis();
        PrefetchingFileIterator pfItertor = new PrefetchingFileIterator(new File("tredbase/src/main/resources/data_small"), 2);
        System.out.println("constructed");
        long length=0;
        while (pfItertor.hasNext()){
            String line =  pfItertor.next();
            if(line != null){
                length += line.length();
            }
        }
        System.out.println(length);
        System.out.println(System.currentTimeMillis() - st);
    }
}
