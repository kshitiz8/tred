package org.tred.base.concurrent.deferredCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestProducer implements  Runnable{
    final DeferredCallback deferredCallback;
    final int maxTime;
    final int delay;

    public TestProducer(DeferredCallback deferredCallback, int maxTime, int delay) {
        this.deferredCallback = deferredCallback;
        this.maxTime = maxTime;
        this.delay = delay;
    }



    public void run(){
        Map<String, String> context = new HashMap<>();
        int tte = Math.abs(new Random().nextInt())%maxTime + delay;
        context.put("tte", ""+tte);
        System.out.println(Thread.currentThread().getName() + ":  added job with time to execution = " + tte + " seconds");
        DeferredCallback.CallBack callBack = new DeferredCallback.CallBack(
                tte,
                (o,u) -> {
                    Map<String, String> map = (Map<String, String>) o;
                    DeferredCallback.CallBack callBack1 =  (DeferredCallback.CallBack)u;
                    System.out.println(Thread.currentThread().getName() + ": Actual Invocation From Consumer: " + callBack1 + " at: " + System.currentTimeMillis() );
                    return null;
                },
                context
        );
        deferredCallback.registerCallback(callBack);
    }



    public static void main(String[] args) throws InterruptedException {
        DeferredCallback dc = new DeferredCallback();
        dc.start();

        ExecutorService producerService = Executors.newFixedThreadPool(5, r->{
            Thread t = new Thread(r,"producers");
            return t;
        }) ;

        System.out.print("queueing jobs ");
        for(int i=0;i<8; i++) {
            producerService.execute(new TestProducer(dc, 10, 10));
        }
        producerService.shutdown();
        try {
            producerService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(3000);
        dc.shutdown(true);

    }

}
