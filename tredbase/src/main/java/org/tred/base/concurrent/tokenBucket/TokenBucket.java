package org.tred.base.concurrent.tokenBucket;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class TokenBucket {

    final BlockingQueue<String> bucket;

    public TokenBucket(int capacity) {
        this.bucket = new ArrayBlockingQueue<String>(capacity);
    }

    public void putToken(String token){
        try {
            bucket.put(token);
        } catch (InterruptedException e) {
        }
    }
    public String getToken(){
        try {
            return bucket.take();
        } catch (InterruptedException e) {
            return null;
        }
    }
}
